package com.smarthome.controller;

import com.smarthome.dto.common.MessageResponse;
import com.smarthome.dto.product.AdminProductDto;
import com.smarthome.dto.product.ProductUpsertRequest;
import com.smarthome.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
  private final ProductService productService;

  public AdminProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  public List<AdminProductDto> list() {
    return productService.listAllForAdmin();
  }

  @PostMapping
  public AdminProductDto create(@Valid @RequestBody ProductUpsertRequest request) {
    return productService.create(request);
  }

  @PutMapping("/{id}")
  public AdminProductDto update(@PathVariable("id") Long id, @Valid @RequestBody ProductUpsertRequest request) {
    return productService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public MessageResponse delete(@PathVariable("id") Long id) {
    productService.delete(id);
    return new MessageResponse("Product deleted successfully");
  }
}
