package com.smarthome.dto.product;

import java.math.BigDecimal;

public class ProductDto {
  private Long id;
  private String name;
  private String description;
  private String technicalSpecs;
  private BigDecimal price;
  private String imageUrl;
  private Integer stock;
  private Long categoryId;
  private String categoryName;
  private BigDecimal averageRating;
  private long reviewCount;

  public ProductDto(
    Long id,
    String name,
    String description,
    String technicalSpecs,
    BigDecimal price,
    String imageUrl,
    Integer stock,
    Long categoryId,
    String categoryName,
    BigDecimal averageRating,
    long reviewCount
  ) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.technicalSpecs = technicalSpecs;
    this.price = price;
    this.imageUrl = imageUrl;
    this.stock = stock;
    this.categoryId = categoryId;
    this.categoryName = categoryName;
    this.averageRating = averageRating;
    this.reviewCount = reviewCount;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getTechnicalSpecs() {
    return technicalSpecs;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public Integer getStock() {
    return stock;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public BigDecimal getAverageRating() {
    return averageRating;
  }

  public long getReviewCount() {
    return reviewCount;
  }
}

