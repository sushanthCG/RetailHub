package com.application.RetailHub.Repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.application.RetailHub.Entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

	 @Query("SELECT oi FROM OrderItem oi WHERE oi.order.order_id = :orderId")
	    List<OrderItem> findByOrderId(@Param("orderId") String orderId);

}