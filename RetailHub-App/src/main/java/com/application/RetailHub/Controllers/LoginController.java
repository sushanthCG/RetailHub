package com.application.RetailHub.Controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.RetailHub.Entities.User;
import com.application.RetailHub.Services.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/auth")
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    // =====================================================
    // 📄 PAGE ROUTES
    // =====================================================
    @GetMapping("/login")
    public String loginPage() {
        return "login";                // ✅ /auth/login → login.html
    }

    @GetMapping("/ForgetPassword")
    public String forgotPasswordPage() {
        return "forgotpassword";       // ✅ /auth/ForgetPassword → ForgetPassword.html
    }

    @GetMapping("/verify")
    public String verifyPage() {
        return "verifyOtp";            // ✅ /auth/verify → verifyOtp.html
    }

    // =====================================================
    // 🔐 NORMAL LOGIN
    // =====================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpServletResponse response) {
        try {
            String token = authService.loginAndGenerateToken(user.getEmail(), user.getPassword());

            Cookie cookie = new Cookie("authToken", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(3600);
            response.addCookie(cookie);

            return ResponseEntity.ok(Map.of("message", "Login successful"));  // ✅ removed sendRedirect

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // 🔑 FORGOT PASSWORD - SEND OTP
    // =====================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendResetOtp(@RequestParam String email) {
        try {
            authService.generatePasswordResetOtp(email);
            return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // ✅ VERIFY RESET OTP
    // =====================================================
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyResetOtp(@RequestParam String email,
                                            @RequestParam String otp) {
        try {
            authService.verifyResetOtp(email, otp);
            return ResponseEntity.ok(Map.of("message", "OTP verified"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // 🔄 RESET PASSWORD
    // =====================================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email,
                                           @RequestParam String newPassword,
                                           @RequestParam String confirmPassword) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match"));
            }
            authService.resetPassword(email, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // ✅ Clear the auth cookie
        Cookie cookie = new Cookie("authToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // ✅ delete cookie
        response.addCookie(cookie);
        return "redirect:/auth/login";  // ✅ back to login
    }
}