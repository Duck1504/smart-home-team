package com.smarthome.service;

import com.smarthome.config.DemoCatalogDefaults;
import com.smarthome.dto.product.AdminProductDto;
import com.smarthome.dto.product.ProductDetailDto;
import com.smarthome.dto.product.ProductDto;
import com.smarthome.dto.product.ProductUpsertRequest;
import com.smarthome.entity.Category;
import com.smarthome.entity.Product;
import com.smarthome.exception.ApiException;
import com.smarthome.repository.CategoryRepository;
import com.smarthome.repository.ProductRepository;
import com.smarthome.repository.PurchaseOrderItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final PurchaseOrderItemRepository purchaseOrderItemRepository;
  private final ProductReviewService productReviewService;

  public ProductService(
    ProductRepository productRepository,
    CategoryRepository categoryRepository,
    PurchaseOrderItemRepository purchaseOrderItemRepository,
    ProductReviewService productReviewService
  ) {
    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
    this.purchaseOrderItemRepository = purchaseOrderItemRepository;
    this.productReviewService = productReviewService;
  }

  @Transactional(readOnly = true)
  public List<ProductDto> listLatest() {
    return toDtos(productRepository.findTop20ByOrderByIdDesc());
  }

  @Transactional(readOnly = true)
  public List<ProductDto> listByCategory(Long categoryId) {
    List<Product> products = productRepository.findAll().stream()
      .filter(product -> product.getCategory() != null && product.getCategory().getId().equals(categoryId))
      .sorted(Comparator.comparing(Product::getId).reversed())
      .toList();
    return toDtos(products);
  }

  @Transactional(readOnly = true)
  public List<AdminProductDto> listAllForAdmin() {
    List<Product> products = productRepository.findAll().stream()
      .sorted(Comparator.comparing(Product::getId).reversed())
      .toList();
    return toAdminDtos(products);
  }

  @Transactional(readOnly = true)
  public ProductDetailDto getById(Long id) {
    Product product = requireProduct(id);
    ProductReviewService.RatingSummary summary = productReviewService
      .getSummariesByProductIds(List.of(product.getId()))
      .getOrDefault(product.getId(), ProductReviewService.emptySummary());

    return toDetailDto(product, summary);
  }

  public AdminProductDto create(ProductUpsertRequest request) {
    Product product = new Product();
    applyRequest(product, request);
    return toAdminDto(productRepository.save(product), ProductReviewService.emptySummary());
  }

  public AdminProductDto update(Long id, ProductUpsertRequest request) {
    Product product = requireProduct(id);
    applyRequest(product, request);
    ProductReviewService.RatingSummary summary = productReviewService
      .getSummariesByProductIds(List.of(product.getId()))
      .getOrDefault(product.getId(), ProductReviewService.emptySummary());
    return toAdminDto(productRepository.save(product), summary);
  }

  public void delete(Long id) {
    Product product = requireProduct(id);

    if (purchaseOrderItemRepository.countByProduct_Id(id) > 0) {
      throw new ApiException(HttpStatus.CONFLICT, "PRODUCT_HAS_ORDERS", "Cannot delete a product that already exists in orders");
    }

    productRepository.delete(product);
  }

  private Product requireProduct(Long id) {
    return productRepository.findById(id)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found"));
  }

  private Category requireCategory(Long categoryId) {
    return categoryRepository.findById(categoryId)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "Category not found"));
  }

  private void applyRequest(Product product, ProductUpsertRequest request) {
    product.setName(request.getName().trim());
    product.setDescription(request.getDescription().trim());
    product.setTechnicalSpecs(normalizeNullableText(request.getTechnicalSpecs()));
    product.setPrice(request.getPrice());
    product.setCostPrice(request.getCostPrice());
    product.setImageUrl(request.getImageUrl() == null ? null : request.getImageUrl().trim());
    product.setStock(request.getStock());
    product.setCategory(requireCategory(request.getCategoryId()));
  }

  private String normalizeNullableText(String value) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private List<ProductDto> toDtos(List<Product> products) {
    Map<Long, ProductReviewService.RatingSummary> summaries = productReviewService.getSummariesByProductIds(
      products.stream().map(Product::getId).toList()
    );

    return products.stream()
      .map(product -> toPublicDto(product, summaries.getOrDefault(product.getId(), ProductReviewService.emptySummary())))
      .toList();
  }

  private List<AdminProductDto> toAdminDtos(List<Product> products) {
    Map<Long, ProductReviewService.RatingSummary> summaries = productReviewService.getSummariesByProductIds(
      products.stream().map(Product::getId).toList()
    );

    return products.stream()
      .map(product -> toAdminDto(product, summaries.getOrDefault(product.getId(), ProductReviewService.emptySummary())))
      .toList();
  }

  private ProductDto toPublicDto(Product product, ProductReviewService.RatingSummary summary) {
    return new ProductDto(
      product.getId(),
      product.getName(),
      product.getDescription(),
      DemoCatalogDefaults.resolveTechnicalSpecs(product),
      product.getPrice(),
      product.getImageUrl(),
      product.getStock(),
      product.getCategory().getId(),
      product.getCategory().getName(),
      summary.getAverageRating(),
      summary.getReviewCount()
    );
  }

  private AdminProductDto toAdminDto(Product product, ProductReviewService.RatingSummary summary) {
    return new AdminProductDto(
      product.getId(),
      product.getName(),
      product.getDescription(),
      DemoCatalogDefaults.resolveTechnicalSpecs(product),
      product.getPrice(),
      DemoCatalogDefaults.resolveCostPrice(product),
      product.getImageUrl(),
      product.getStock(),
      product.getCategory().getId(),
      product.getCategory().getName(),
      summary.getAverageRating(),
      summary.getReviewCount()
    );
  }

  private ProductDetailDto toDetailDto(Product product, ProductReviewService.RatingSummary summary) {
    return new ProductDetailDto(
      product.getId(),
      product.getName(),
      product.getDescription(),
      DemoCatalogDefaults.resolveTechnicalSpecs(product),
      product.getPrice(),
      product.getImageUrl(),
      product.getStock(),
      product.getCategory().getId(),
      product.getCategory().getName(),
      summary.getAverageRating(),
      summary.getReviewCount(),
      productReviewService.listByProductId(product.getId())
    );
  }
}
