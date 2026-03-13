package com.application.RetailHub.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.application.RetailHub.Repositories.ProductRepository;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/products-page")
    public String showProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "products";
    }
}
