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
@Table(name="otp")
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private int otpId;

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // FK column in otp table
    private User user;

    public Otp() {
        super();
    }


    public Otp(int otpId, String otpCode, LocalDateTime createdAt, LocalDateTime expiresAt, User user) {
		super();
		this.otpId = otpId;
		this.otpCode = otpCode;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
		this.user = user;
	}


	public int getOtpId() {
		return otpId;
	}


	public void setOtpId(int otpId) {
		this.otpId = otpId;
	}


	public String getOtpCode() {
		return otpCode;
	}


	public void setOtpCode(String otpCode) {
		this.otpCode = otpCode;
	}


	public LocalDateTime getCreatedAt() {
		return createdAt;
	}


	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}


	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}


	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


}