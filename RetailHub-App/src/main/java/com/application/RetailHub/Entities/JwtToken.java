package com.application.RetailHub.Entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity
@Table(name = "jwt_tokens")
public class JwtToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer token_id;

    @Column(length = 512)
    private String token;

    private LocalDateTime created_at;
    private LocalDateTime expires_at;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    public JwtToken() {
		super();
	}

	public JwtToken(User user, String token, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.created_at = LocalDateTime.now();
        this.expires_at = expiresAt;
    }

	public JwtToken(Integer token_id, String token, LocalDateTime created_at, LocalDateTime expires_at, User user) {
		super();
		this.token_id = token_id;
		this.token = token;
		this.created_at = created_at;
		this.expires_at = expires_at;
		this.user = user;
	}

	public Integer getToken_id() {
		return token_id;
	}

	public void setToken_id(Integer token_id) {
		this.token_id = token_id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public LocalDateTime getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDateTime created_at) {
		this.created_at = created_at;
	}

	public LocalDateTime getExpires_at() {
		return expires_at;
	}

	public void setExpires_at(LocalDateTime expires_at) {
		this.expires_at = expires_at;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}