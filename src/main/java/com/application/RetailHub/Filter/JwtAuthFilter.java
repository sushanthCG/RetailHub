package com.application.RetailHub.Filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.application.RetailHub.Entities.JwtToken;
import com.application.RetailHub.Repositories.JwtTokenRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final JwtTokenRepository jwtTokenRepository;

    public JwtAuthFilter(JwtTokenRepository jwtTokenRepository) {
        this.jwtTokenRepository = jwtTokenRepository;
    }

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/auth/login", "/auth/logout", "/auth/ForgetPassword",
        "/auth/forgot-password", "/auth/verify", "/auth/verify-reset-otp",
        "/auth/reset-password", "/api/register", "/api/",
        "/style.css", "/login.js", "/register.js",
        "/ForgetPassword.js", "/verifyOtp.js",
        "/order.css", "/order.js",
        "/customerhome.css", "/customerhome.js",
        "/products.css", "/orders.css"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        for (String pub : PUBLIC_PATHS) {
            if (path.equals(pub)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String token = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("authToken".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            response.sendRedirect("/auth/login?reason=unauthorized");
            return;
        }

        try {
            byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            var key = Keys.hmacShaKeyFor(keyBytes);

            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

            JwtToken dbToken = jwtTokenRepository.findByToken(token);
            if (dbToken == null) {
                response.sendRedirect("/auth/login?reason=unauthorized");
                return;
            }

            if (dbToken.getExpires_at() != null &&
                dbToken.getExpires_at().isBefore(java.time.LocalDateTime.now())) {
                jwtTokenRepository.delete(dbToken);
                response.sendRedirect("/auth/login?reason=expired");
                return;
            }

            request.setAttribute("username", claims.getSubject());
            request.setAttribute("role", claims.get("role", String.class));

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            response.sendRedirect("/auth/login?reason=expired");
        } catch (JwtException e) {
            response.sendRedirect("/auth/login?reason=unauthorized");
        }
    }
}