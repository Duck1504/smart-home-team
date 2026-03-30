package com.smarthome.dto.product;

import java.math.BigDecimal;

public class AdminProductDto extends ProductDto {
  private final BigDecimal costPrice;

  public AdminProductDto(
    Long id,
    String name,
    String description,
    String technicalSpecs,
    BigDecimal price,
    BigDecimal costPrice,
    String imageUrl,
    Integer stock,
    Long categoryId,
    String categoryName,
    BigDecimal averageRating,
    long reviewCount
  ) {
    super(id, name, description, technicalSpecs, price, imageUrl, stock, categoryId, categoryName, averageRating, reviewCount);
    this.costPrice = costPrice;
  }

  public BigDecimal getCostPrice() {
    return costPrice;
  }
}
