package com.application.RetailHub.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.application.RetailHub.Entities.CartItem;

public interface CartRepository extends JpaRepository<CartItem, Integer> {

	@Query("SELECT c FROM CartItem c WHERE c.user.user_id = :userId")
    List<CartItem> findByUserId(@Param("userId") Integer userId);


}