package com.smarthome.service;

import com.smarthome.dto.category.CategoryDto;
import com.smarthome.dto.category.CategoryRequest;
import com.smarthome.entity.Category;
import com.smarthome.exception.ApiException;
import com.smarthome.repository.CategoryRepository;
import com.smarthome.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;

  public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
    this.categoryRepository = categoryRepository;
    this.productRepository = productRepository;
  }

  public List<CategoryDto> listAll() {
    return categoryRepository.findAll().stream()
      .sorted(Comparator.comparing(Category::getName))
      .map(this::toDto)
      .toList();
  }

  public CategoryDto create(CategoryRequest request) {
    String normalizedName = request.getName().trim();
    ensureNameAvailable(normalizedName, null);

    Category category = new Category();
    category.setName(normalizedName);
    return toDto(categoryRepository.save(category));
  }

  public CategoryDto update(Long id, CategoryRequest request) {
    Category category = requireEntity(id);
    String normalizedName = request.getName().trim();
    ensureNameAvailable(normalizedName, id);

    category.setName(normalizedName);
    return toDto(categoryRepository.save(category));
  }

  public void delete(Long id) {
    Category category = requireEntity(id);
    if (productRepository.countByCategory_Id(id) > 0) {
      throw new ApiException(HttpStatus.CONFLICT, "CATEGORY_HAS_PRODUCTS", "Cannot delete a category that still has products");
    }
    categoryRepository.delete(category);
  }

  public Category requireEntity(Long id) {
    return categoryRepository.findById(id)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "Category not found"));
  }

  private void ensureNameAvailable(String name, Long currentId) {
    boolean exists = categoryRepository.findAll().stream()
      .anyMatch(category -> category.getName() != null
        && category.getName().equalsIgnoreCase(name)
        && (currentId == null || !category.getId().equals(currentId)));

    if (exists) {
      throw new ApiException(HttpStatus.CONFLICT, "CATEGORY_NAME_TAKEN", "Category name already exists");
    }
  }

  private CategoryDto toDto(Category category) {
    return new CategoryDto(
      category.getId(),
      category.getName(),
      productRepository.countByCategory_Id(category.getId())
    );
  }
}
