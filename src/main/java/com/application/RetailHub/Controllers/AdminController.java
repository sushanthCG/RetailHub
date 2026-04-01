package com.application.RetailHub.Controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.application.RetailHub.Entities.Order;
import com.application.RetailHub.Entities.Product;
import com.application.RetailHub.Repositories.OrderRepository;
import com.application.RetailHub.Repositories.ProductRepository;
import com.application.RetailHub.Repositories.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderRepository   orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository    userRepository;

    public AdminController(OrderRepository orderRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository) {
        this.orderRepository   = orderRepository;
        this.productRepository = productRepository;
        this.userRepository    = userRepository;
    }

    private boolean isAdmin(HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            response.sendRedirect("/auth/login?reason=unauthorized");
            return false;
        }
        return true;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        if (!isAdmin(request, response)) return null;
        return "admin";
    }

    @GetMapping("/orders")
    @ResponseBody
    public ResponseEntity<?> getAllOrders(HttpServletRequest request,
                                          HttpServletResponse response) throws Exception {
        if (!isAdmin(request, response)) return null;

        List<Map<String, Object>> result = orderRepository.findAll().stream().map(order ->
            Map.of(
                "orderId",     (Object) order.getOrder_id(),
                "status",      order.getStatus().name(),
                "totalAmount", order.getTotal_amount(),
                "createdAt",   order.getCreated_at().toString(),
                "username",    order.getUser() != null ? order.getUser().getUsername() : "N/A",
                "email",       order.getUser() != null ? order.getUser().getEmail() : "N/A",
                "items",       order.getItems().stream().map(item -> Map.of(
                    "productName", item.getProduct().getName(),
                    "qty",         item.getQuantity(),
                    "price",       item.getPrice_per_unit(),
                    "total",       item.getTotal_price()
                )).collect(Collectors.toList())
            )
        ).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PutMapping("/orders/{orderId}/status")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderId,
                                                @RequestBody Map<String, String> body,
                                                HttpServletRequest request,
                                                HttpServletResponse response) throws Exception {
        if (!isAdmin(request, response)) return null;

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String newStatus = body.get("status");
        order.setStatus(Order.Status.valueOf(newStatus));
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of("message", "Order status updated to " + newStatus));
    }

    @GetMapping("/products")
    @ResponseBody
    public ResponseEntity<?> getAllProducts(HttpServletRequest request,
                                             HttpServletResponse response) throws Exception {
        if (!isAdmin(request, response)) return null;

        List<Map<String, Object>> result = productRepository.findAll().stream().map(p ->
            Map.of(
                "productId",   (Object) p.getProduct_id(),
                "name",        p.getName(),
                "description", p.getDescription() != null ? p.getDescription() : "",
                "price",       p.getPrice(),
                "stock",       p.getStock(),
                "category",    p.getCategory() != null ? p.getCategory().getCategory_name() : "N/A",
                "image",       (p.getImages() != null && !p.getImages().isEmpty())
                                   ? p.getImages().get(0).getImage_url() : ""
            )
        ).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PutMapping("/products/{productId}")
    @ResponseBody
    public ResponseEntity<?> updateProduct(@PathVariable Integer productId,
                                            @RequestBody Map<String, Object> body,
                                            HttpServletRequest request,
                                            HttpServletResponse response) throws Exception {
        if (!isAdmin(request, response)) return null;

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (body.containsKey("name"))
            product.setName(body.get("name").toString());
        if (body.containsKey("description"))
            product.setDescription(body.get("description").toString());
        if (body.containsKey("price"))
            product.setPrice(Double.parseDouble(body.get("price").toString()));
        if (body.containsKey("stock"))
            product.setStock(Integer.parseInt(body.get("stock").toString()));

        productRepository.save(product);
        return ResponseEntity.ok(Map.of("message", "Product updated"));
    }

    @DeleteMapping("/products/{productId}")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(@PathVariable Integer productId,
                                            HttpServletRequest request,
                                            HttpServletResponse response) throws Exception {
        if (!isAdmin(request, response)) return null;

        productRepository.deleteById(productId);
        return ResponseEntity.ok(Map.of("message", "Product deleted"));
    }

    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<?> getAllUsers(HttpServletRequest request,
                                          HttpServletResponse response) throws Exception {
        if (!isAdmin(request, response)) return null;

        List<Map<String, Object>> result = userRepository.findAll().stream().map(u ->
            Map.of(
                "userId",    (Object) u.getUser_id(),
                "username",  u.getUsername(),
                "email",     u.getEmail(),
                "role",      u.getRole().name(),
                "createdAt", u.getCreated_at() != null ? u.getCreated_at().toString() : ""
            )
        ).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getStats(HttpServletRequest request,
                                       HttpServletResponse response) throws Exception {
        if (!isAdmin(request, response)) return null;

        long totalOrders   = orderRepository.count();
        long totalProducts = productRepository.count();
        long totalUsers    = userRepository.count();

        double totalRevenue = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == Order.Status.SUCCESS)
                .mapToDouble(Order::getTotal_amount)
                .sum();

        long pendingOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == Order.Status.PENDING)
                .count();

        return ResponseEntity.ok(Map.of(
            "totalOrders",   totalOrders,
            "totalProducts", totalProducts,
            "totalUsers",    totalUsers,
            "totalRevenue",  totalRevenue,
            "pendingOrders", pendingOrders
        ));
    }
}