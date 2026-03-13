package com.application.RetailHub.Entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer user_id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    // Relationships
    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    @OneToMany(mappedBy = "user")
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "user")
    private List<JwtToken> tokens;

    // ENUM
    public enum Role {
        ADMIN,
        CUSTOMER
    }

    // 🔥 AUTOMATIC DEFAULT VALUES
    @PrePersist
    public void prePersist() {
        if (this.role == null) {
            this.role = Role.CUSTOMER;   // 🔥 Automatically set CUSTOMER
        }
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
    }

    // 🔥 Automatically update time on update
    @PreUpdate
    public void preUpdate() {
        this.updated_at = LocalDateTime.now();
    }

    // Constructors
    public User() {}

    // Getters & Setters
    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public List<JwtToken> getTokens() {
        return tokens;
    }
}