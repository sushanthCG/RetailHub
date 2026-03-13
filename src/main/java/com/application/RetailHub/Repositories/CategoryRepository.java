package com.application.RetailHub.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.application.RetailHub.Entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

}