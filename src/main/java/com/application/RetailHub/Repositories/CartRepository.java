package com.application.RetailHub.Repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.application.RetailHub.Entities.CartItem;
import jakarta.transaction.Transactional;

public interface CartRepository extends JpaRepository<CartItem, Integer> {

    @Query("SELECT c FROM CartItem c WHERE c.user.user_id = :userId")
    List<CartItem> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT c FROM CartItem c WHERE c.user.user_id = :userId AND c.product.product_id = :productId")
    CartItem findByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.user.user_id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);
}