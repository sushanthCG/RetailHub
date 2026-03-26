package com.application.RetailHub.Controllers;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.application.RetailHub.Entities.User;
import com.application.RetailHub.Repositories.ProductRepository;
import com.application.RetailHub.Services.UserService;

@Controller
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final ProductRepository productRepository;

    public UserController(UserService userService, ProductRepository productRepository) {
        this.userService = userService;
        this.productRepository = productRepository;
    }


    @GetMapping("/")
    public String homePage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/customerhome")
    public String customerHome(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "customerhome";
    }

    @GetMapping("/products-page")
    public String productsPage(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products";
    }
    
    @GetMapping("/my-orders")
    public String myOrdersPage() {
        return "myorders"; 
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.UserRegister(user);
            return ResponseEntity.ok(
                Map.of(
                    "message", "User registered successfully",
                    "username", registeredUser.getUsername(),
                    "email", registeredUser.getEmail(),
                    "role", registeredUser.getRole().name()
                )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}