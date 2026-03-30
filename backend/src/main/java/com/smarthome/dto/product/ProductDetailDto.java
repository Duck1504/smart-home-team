package com.smarthome.dto.product;

import java.math.BigDecimal;
import java.util.List;

public class ProductDetailDto extends ProductDto {
  private List<ProductReviewDto> reviews;

  public ProductDetailDto(
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
    long reviewCount,
    List<ProductReviewDto> reviews
  ) {
    super(id, name, description, technicalSpecs, price, imageUrl, stock, categoryId, categoryName, averageRating, reviewCount);
    this.reviews = reviews;
  }

  public List<ProductReviewDto> getReviews() {
    return reviews;
  }
}
