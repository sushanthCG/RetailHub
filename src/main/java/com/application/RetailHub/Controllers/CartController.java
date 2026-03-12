package com.application.RetailHub.Controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.RetailHub.Entities.CartItem;
import com.application.RetailHub.Entities.Product;
import com.application.RetailHub.Entities.User;
import com.application.RetailHub.Repositories.CartRepository;
import com.application.RetailHub.Repositories.ProductRepository;
import com.application.RetailHub.Repositories.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartController(CartRepository cartItemRepository,
                          ProductRepository productRepository,
                          UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository  = productRepository;
        this.userRepository     = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getCart(HttpServletRequest request) {
        try {
            User user = getUser(request);
            List<CartItem> items = cartItemRepository.findByUserId(user.getUser_id());

            List<Map<String, Object>> result = items.stream().map(item -> Map.of(
                "id",         (Object) item.getProduct().getProduct_id(),
                "name",       item.getProduct().getName(),
                "price",      item.getProduct().getPrice(),
                "img",        item.getProduct().getImages() != null
                                && !item.getProduct().getImages().isEmpty()
                                ? item.getProduct().getImages().get(0).getImage_url() : "",
                "qty",        item.getQuantity(),
                "cartItemId", item.getId()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> body,
                                       HttpServletRequest request) {
        try {
            User    user      = getUser(request);
            Integer productId = Integer.parseInt(body.get("productId").toString());
            Integer qty       = body.containsKey("qty")
                                ? Integer.parseInt(body.get("qty").toString()) : 1;

            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

            CartItem existing = cartItemRepository
                .findByUserIdAndProductId(user.getUser_id(), productId);

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + qty);
                cartItemRepository.save(existing);
            } else {
                CartItem item = new CartItem();
                item.setUser(user);
                item.setProduct(product);
                item.setQuantity(qty);
                cartItemRepository.save(item);
            }

            return ResponseEntity.ok(Map.of("message", "Added to cart"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateQty(@RequestBody Map<String, Object> body,
                                       HttpServletRequest request) {
        try {
            User    user      = getUser(request);
            Integer productId = Integer.parseInt(body.get("productId").toString());
            Integer qty       = Integer.parseInt(body.get("qty").toString());

            CartItem item = cartItemRepository
                .findByUserIdAndProductId(user.getUser_id(), productId);

            if (item == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Item not in cart"));
            }

            if (qty <= 0) {
                cartItemRepository.delete(item);
            } else {
                item.setQuantity(qty);
                cartItemRepository.save(item);
            }

            return ResponseEntity.ok(Map.of("message", "Cart updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeItem(@PathVariable Integer productId,
                                        HttpServletRequest request) {
        try {
            User     user = getUser(request);
            CartItem item = cartItemRepository
                .findByUserIdAndProductId(user.getUser_id(), productId);

            if (item != null) {
                cartItemRepository.delete(item);
            }

            return ResponseEntity.ok(Map.of("message", "Removed from cart"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest request) {
        try {
            User user = getUser(request);
            cartItemRepository.deleteByUserId(user.getUser_id());
            return ResponseEntity.ok(Map.of("message", "Cart cleared"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private User getUser(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}