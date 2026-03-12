package com.application.RetailHub.Services;

import java.util.List;
import org.springframework.stereotype.Service;
import com.application.RetailHub.Entities.ProductImages;
import com.application.RetailHub.Repositories.ProductImageRepository;

@Service
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;

    public ProductImageServiceImpl(ProductImageRepository productImageRepository) {
        this.productImageRepository = productImageRepository;
    }

    @Override
    public List<ProductImages> getAllImages() {
        return productImageRepository.findAll();
    }
}