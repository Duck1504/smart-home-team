package com.smarthome.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductUpsertRequest {
  @NotBlank
  @Size(max = 150)
  private String name;

  @NotBlank
  @Size(max = 2000)
  private String description;

  @Size(max = 4000)
  private String technicalSpecs;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = false)
  private BigDecimal price;

  @NotNull
  @DecimalMin(value = "0.0", inclusive = true)
  private BigDecimal costPrice;

  @Size(max = 500)
  private String imageUrl;

  @NotNull
  @Min(0)
  private Integer stock;

  @NotNull
  private Long categoryId;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTechnicalSpecs() {
    return technicalSpecs;
  }

  public void setTechnicalSpecs(String technicalSpecs) {
    this.technicalSpecs = technicalSpecs;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public BigDecimal getCostPrice() {
    return costPrice;
  }

  public void setCostPrice(BigDecimal costPrice) {
    this.costPrice = costPrice;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public Integer getStock() {
    return stock;
  }

  public void setStock(Integer stock) {
    this.stock = stock;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }
}
