package com.application.RetailHub.Repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.application.RetailHub.Entities.Otp;

import jakarta.transaction.Transactional;

public interface OtpRepository extends JpaRepository<Otp, Integer> {

    @Query("SELECT o FROM Otp o WHERE o.user.user_id = :userId ORDER BY o.createdAt DESC LIMIT 1")
    Optional<Otp> findLatestOtpByUserId(@Param("userId") Integer userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Otp o WHERE o.user.user_id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);

}