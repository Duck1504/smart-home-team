package com.smarthome.controller;

import com.smarthome.dto.product.ProductDetailDto;
import com.smarthome.dto.product.ProductDto;
import com.smarthome.dto.product.ProductReviewDto;
import com.smarthome.dto.product.ProductReviewRequest;
import com.smarthome.service.ProductService;
import com.smarthome.service.ProductReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  private final ProductService productService;
  private final ProductReviewService productReviewService;

  public ProductController(ProductService productService, ProductReviewService productReviewService) {
    this.productService = productService;
    this.productReviewService = productReviewService;
  }

  @GetMapping
  public List<ProductDto> list(
    @RequestParam(name = "categoryId", required = false) Long categoryId
  ) {
    if (categoryId != null) {
      return productService.listByCategory(categoryId);
    }
    return productService.listLatest();
  }

  @GetMapping("/{id}")
  public ProductDetailDto getById(@PathVariable("id") Long id) {
    return productService.getById(id);
  }

  @PostMapping("/{id}/reviews")
  @PreAuthorize("isAuthenticated()")
  @ResponseStatus(HttpStatus.CREATED)
  public ProductReviewDto createReview(
    @PathVariable("id") Long id,
    @Valid @RequestBody ProductReviewRequest request
  ) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return productReviewService.upsertReview(id, username, request);
  }
}

