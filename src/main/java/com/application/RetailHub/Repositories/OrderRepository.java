package com.application.RetailHub.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.application.RetailHub.Entities.Order;

public interface OrderRepository extends JpaRepository<Order, String> {


    @Query("SELECT o FROM Order o WHERE o.user.user_id = :userId")
    List<Order> findByUserId(@Param("userId") Integer userId);
   
    
 @Query("SELECT o FROM Order o WHERE o.user.user_id = :userId ORDER BY o.created_at DESC")
 List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Integer userId);
}