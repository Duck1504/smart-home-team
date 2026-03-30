package com.smarthome.dto.stats;

import java.math.BigDecimal;

public class CategorySalesDto {
  private final Long categoryId;
  private final String categoryName;
  private final long soldQuantity;
  private final BigDecimal revenue;
  private final BigDecimal expense;
  private final BigDecimal profit;

  public CategorySalesDto(
    Long categoryId,
    String categoryName,
    long soldQuantity,
    BigDecimal revenue,
    BigDecimal expense,
    BigDecimal profit
  ) {
    this.categoryId = categoryId;
    this.categoryName = categoryName;
    this.soldQuantity = soldQuantity;
    this.revenue = revenue;
    this.expense = expense;
    this.profit = profit;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public String getCategoryName() {
    return categoryName;
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
