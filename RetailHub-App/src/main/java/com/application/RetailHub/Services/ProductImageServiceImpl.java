package com.application.RetailHub.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.application.RetailHub.Entities.ProductImages;
import com.application.RetailHub.Repositories.ProductImageRepository;

@Service
public class ProductImageServiceImpl implements ProductImageService {

    @Autowired
    private ProductImageRepository productImageRepository;

    @Override
    public List<ProductImages> getAllImages() {
        return productImageRepository.findAll();
    }
}
