package com.application.RetailHub.Repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.application.RetailHub.Entities.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.category.category_id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Integer categoryId);

}