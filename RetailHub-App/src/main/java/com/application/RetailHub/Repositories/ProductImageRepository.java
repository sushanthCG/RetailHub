package com.application.RetailHub.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.application.RetailHub.Entities.ProductImages;

public interface ProductImageRepository extends JpaRepository<ProductImages, Integer> {

}