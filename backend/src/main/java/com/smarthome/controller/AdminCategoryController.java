package com.smarthome.controller;

import com.smarthome.dto.category.CategoryDto;
import com.smarthome.dto.category.CategoryRequest;
import com.smarthome.dto.common.MessageResponse;
import com.smarthome.service.CategoryService;
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
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {
  private final CategoryService categoryService;

  public AdminCategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping
  public List<CategoryDto> list() {
    return categoryService.listAll();
  }

  @PostMapping
  public CategoryDto create(@Valid @RequestBody CategoryRequest request) {
    return categoryService.create(request);
  }

  @PutMapping("/{id}")
  public CategoryDto update(@PathVariable("id") Long id, @Valid @RequestBody CategoryRequest request) {
    return categoryService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public MessageResponse delete(@PathVariable("id") Long id) {
    categoryService.delete(id);
    return new MessageResponse("Category deleted successfully");
  }
}
