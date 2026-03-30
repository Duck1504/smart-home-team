package com.smarthome.dto.stats;

import java.math.BigDecimal;

public class ProductSalesDto {
  private final Long productId;
  private final String productName;
  private final String categoryName;
  private final Integer stock;
  private final long soldQuantity;
  private final BigDecimal revenue;
  private final BigDecimal expense;
  private final BigDecimal profit;

  public ProductSalesDto(
    Long productId,
    String productName,
    String categoryName,
    Integer stock,
    long soldQuantity,
    BigDecimal revenue,
    BigDecimal expense,
    BigDecimal profit
  ) {
    this.productId = productId;
    this.productName = productName;
    this.categoryName = categoryName;
    this.stock = stock;
    this.soldQuantity = soldQuantity;
    this.revenue = revenue;
    this.expense = expense;
    this.profit = profit;
  }

  public Long getProductId() {
    return productId;
  }

  public String getProductName() {
    return productName;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public Integer getStock() {
    return stock;
  }

  public long getSoldQuantity() {
    return soldQuantity;
  }

  public BigDecimal getRevenue() {
    return revenue;
  }

  public BigDecimal getExpense() {
    return expense;
  }

  public BigDecimal getProfit() {
    return profit;
  }
}
