package com.application.RetailHub.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.application.RetailHub.Entities.JwtToken;

public interface JwtTokenRepository extends JpaRepository<JwtToken, Integer> {

    @Query("SELECT t FROM JwtToken t WHERE t.user.user_id = :user_id")
    JwtToken findByUserId(@Param("user_id") Integer user_id);

    @Query("SELECT t FROM JwtToken t WHERE t.token = :token")
    JwtToken findByToken(@Param("token") String token);
}