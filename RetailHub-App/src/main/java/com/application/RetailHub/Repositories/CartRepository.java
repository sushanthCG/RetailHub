package com.application.RetailHub.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.application.RetailHub.Entities.CartItem;

public interface CartRepository extends JpaRepository<CartItem, Integer> {

    // Get all cart items for a user
    @Query("SELECT c FROM CartItem c WHERE c.user.user_id = :userId")
    List<CartItem> findByUserId(@Param("userId") Integer userId);

    // Find specific cart item by user + product
    @Query("SELECT c FROM CartItem c WHERE c.user.user_id = :userId AND c.product.product_id = :productId")
    CartItem findByUserIdAndProductId(@Param("userId") Integer userId,
                                      @Param("productId") Integer productId);

    // Delete all cart items for a user (after order placed)
    @Query("DELETE FROM CartItem c WHERE c.user.user_id = :userId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByUserId(@Param("userId") Integer userId);
}