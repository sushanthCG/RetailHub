package com.application.RetailHub.Controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.application.RetailHub.Entities.Order;
import com.application.RetailHub.Entities.OrderItem;
import com.application.RetailHub.Entities.Product;
import com.application.RetailHub.Entities.User;
import com.application.RetailHub.Repositories.CartRepository;
import com.application.RetailHub.Repositories.OrderItemRepository;
import com.application.RetailHub.Repositories.OrderRepository;
import com.application.RetailHub.Repositories.ProductRepository;
import com.application.RetailHub.Repositories.UserRepository;
import com.razorpay.RazorpayClient;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/api")
public class OrderController {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private final OrderRepository     orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository   productRepository;
    private final UserRepository      userRepository;
    private final CartRepository      cartRepository;

    public OrderController(OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository,
                           CartRepository cartRepository) {
        this.orderRepository     = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository   = productRepository;
        this.userRepository      = userRepository;
        this.cartRepository      = cartRepository;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        model.addAttribute("razorpayKeyId", razorpayKeyId);
        return "order";
    }

    @PostMapping("/create-razorpay-order")
    @ResponseBody
    public ResponseEntity<?> createRazorpayOrder(@RequestBody Map<String, Object> body) {
        try {
            int amountPaise = (int)(Double.parseDouble(body.get("amount").toString()) * 100);

            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject options = new JSONObject();
            options.put("amount",   amountPaise);
            options.put("currency", "INR");
            options.put("receipt",  "order_" + UUID.randomUUID().toString().substring(0, 8));

            com.razorpay.Order rzpOrder = client.orders.create(options);

            return ResponseEntity.ok(Map.of(
                "razorpay_order_id", rzpOrder.get("id").toString(),
                "amount",            amountPaise,
                "currency",          "INR"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/save-order")
    @ResponseBody
    public ResponseEntity<?> saveOrder(@RequestBody Map<String, Object> body,
                                       HttpServletRequest request) {
        try {
            String username = (String) request.getAttribute("username");
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Map<String, Object>> items =
                    (List<Map<String, Object>>) body.get("items");

            for (Map<String, Object> item : items) {
                Integer productId = Integer.parseInt(item.get("id").toString());
                Integer qty       = Integer.parseInt(item.get("qty").toString());

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                if (product.getStock() < qty) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Insufficient stock for: "
                                    + product.getName()));
                }
            }

            String paymentMethod = body.get("paymentMethod") != null
                    ? body.get("paymentMethod").toString() : "";

            if ("RAZORPAY".equals(paymentMethod)) {
                String rzpOrderId   = body.get("razorpayOrderId").toString();
                String rzpPaymentId = body.get("paymentId").toString();
                String rzpSignature = body.get("razorpaySignature").toString();

                String payload = rzpOrderId + "|" + rzpPaymentId;
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(), "HmacSHA256"));
                byte[] hash = mac.doFinal(payload.getBytes());
                StringBuilder computed = new StringBuilder();
                for (byte b : hash) computed.append(String.format("%02x", b));

                if (!computed.toString().equals(rzpSignature)) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Payment verification failed"));
                }
            }


            Order order = new Order();
            order.setOrder_id("ORD-" + UUID.randomUUID().toString()
                    .substring(0, 10).toUpperCase());
            order.setTotal_amount(Double.parseDouble(
                    body.get("totalAmount").toString()));
            order.setStatus("COD".equals(paymentMethod) 
            	    ? Order.Status.PENDING 
            	    : Order.Status.SUCCESS);
            order.setCreated_at(LocalDateTime.now());
            order.setUpdated_at(LocalDateTime.now());
            order.setUser(user);
            orderRepository.save(order);

            for (Map<String, Object> item : items) {
                Integer productId = Integer.parseInt(item.get("id").toString());
                Integer qty       = Integer.parseInt(item.get("qty").toString());
                Double  price     = Double.parseDouble(item.get("price").toString());

                Product product = productRepository.findById(productId).get();
                product.setStock(product.getStock() - qty);
                productRepository.save(product);

                OrderItem oi = new OrderItem();
                oi.setOrder(order);
                oi.setProduct(product);
                oi.setQuantity(qty);
                oi.setPrice_per_unit(price);
                oi.setTotal_price(price * qty);
                orderItemRepository.save(oi);
            }

            cartRepository.deleteByUserId(user.getUser_id());

            return ResponseEntity.ok(Map.of(
                "message",  "Order placed successfully",
                "order_id", order.getOrder_id()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/my-orders-data")
    @ResponseBody
    public ResponseEntity<?> getMyOrders(HttpServletRequest request) {
        try {
            String username = (String) request.getAttribute("username");
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(
                    user.getUser_id());

            List<Map<String, Object>> result = orders.stream().map(order -> Map.of(
                "orderId",     (Object) order.getOrder_id(),
                "totalAmount", order.getTotal_amount(),
                "status",      order.getStatus().name(),
                "createdAt",   order.getCreated_at().toString(),
                "items",       order.getItems().stream().map(item -> Map.of(
                    "productName", item.getProduct().getName(),
                    "qty",         item.getQuantity(),
                    "price",       item.getPrice_per_unit(),
                    "total",       item.getTotal_price()
                )).collect(java.util.stream.Collectors.toList())
            )).collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}