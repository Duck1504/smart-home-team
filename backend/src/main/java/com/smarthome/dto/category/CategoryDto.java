package com.smarthome.dto.category;

public class CategoryDto {
  private final Long id;
  private final String name;
  private final long productCount;

  public CategoryDto(Long id, String name, long productCount) {
    this.id = id;
    this.name = name;
    this.productCount = productCount;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public long getProductCount() {
    return productCount;
  }
}
