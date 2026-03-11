package com.application.RetailHub.Controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private final OrderRepository      orderRepository;
    private final OrderItemRepository  orderItemRepository;
    private final ProductRepository    productRepository;
    private final UserRepository       userRepository;

    public OrderController(OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository) {
        this.orderRepository     = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository   = productRepository;
        this.userRepository      = userRepository;
    }

    // ── Checkout PAGE ──────────────────────────────
    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        model.addAttribute("razorpayKeyId", razorpayKeyId);
        return "order";
    }

    // ── Create Razorpay Order (called from JS) ──────
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

    // ── Save Order after Payment Success ───────────
    @PostMapping("/save-order")
    @ResponseBody
    public ResponseEntity<?> saveOrder(@RequestBody Map<String, Object> body,
                                       HttpServletRequest request) {
        try {
            // Get username from JWT filter attribute
            String username = (String) request.getAttribute("username");
            User user = userRepository.findByUsername(username);

            // Build Order
            Order order = new Order();
            order.setOrder_id("ORD-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
            order.setTotal_amount(Double.parseDouble(body.get("totalAmount").toString()));
            order.setStatus(Order.Status.SUCCESS);
            order.setCreated_at(LocalDateTime.now());
            order.setUpdated_at(LocalDateTime.now());
            order.setUser(user);
            orderRepository.save(order);

            // Save each OrderItem
            List<Map<String, Object>> items =
                (List<Map<String, Object>>) body.get("items");

            for (Map<String, Object> item : items) {
                Integer productId = Integer.parseInt(item.get("id").toString());
                Integer qty       = Integer.parseInt(item.get("qty").toString());
                Double  price     = Double.parseDouble(item.get("price").toString());

                Product product = productRepository.findById(productId).orElse(null);
                if (product == null) continue;

                // Reduce stock
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

            return ResponseEntity.ok(Map.of(
                "message",  "Order placed successfully",
                "order_id", order.getOrder_id()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}