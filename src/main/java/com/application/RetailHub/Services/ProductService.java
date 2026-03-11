package com.application.RetailHub.Services;

import java.util.List;

import com.application.RetailHub.Entities.Product;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Integer id);
}
