package com.smarthome.repository;

import com.smarthome.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
  List<Product> findTop20ByOrderByIdDesc();
  long countByCategory_Id(Long categoryId);
}

