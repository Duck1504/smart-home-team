package com.smarthome.dto.stats;

import java.math.BigDecimal;
import java.util.List;

public class AdminStatsDto {
  private final long totalOrders;
  private final long totalUnitsSold;
  private final BigDecimal totalRevenue;
  private final BigDecimal totalExpense;
  private final BigDecimal totalProfit;
  private final long totalProducts;
  private final long lowStockProducts;
  private final List<ProductSalesDto> productSales;
  private final List<CategorySalesDto> categorySales;
  private final List<RecentOrderDto> recentOrders;

  public AdminStatsDto(
    long totalOrders,
    long totalUnitsSold,
    BigDecimal totalRevenue,
    BigDecimal totalExpense,
    BigDecimal totalProfit,
    long totalProducts,
    long lowStockProducts,
    List<ProductSalesDto> productSales,
    List<CategorySalesDto> categorySales,
    List<RecentOrderDto> recentOrders
  ) {
    this.totalOrders = totalOrders;
    this.totalUnitsSold = totalUnitsSold;
    this.totalRevenue = totalRevenue;
    this.totalExpense = totalExpense;
    this.totalProfit = totalProfit;
    this.totalProducts = totalProducts;
    this.lowStockProducts = lowStockProducts;
    this.productSales = productSales;
    this.categorySales = categorySales;
    this.recentOrders = recentOrders;
  }

  public long getTotalOrders() {
    return totalOrders;
  }

  public long getTotalUnitsSold() {
    return totalUnitsSold;
  }

  public BigDecimal getTotalRevenue() {
    return totalRevenue;
  }

  public BigDecimal getTotalExpense() {
    return totalExpense;
  }

  public BigDecimal getTotalProfit() {
    return totalProfit;
  }

  public long getTotalProducts() {
    return totalProducts;
  }

  public long getLowStockProducts() {
    return lowStockProducts;
  }

  public List<ProductSalesDto> getProductSales() {
    return productSales;
  }

  public List<CategorySalesDto> getCategorySales() {
    return categorySales;
  }

  public List<RecentOrderDto> getRecentOrders() {
    return recentOrders;
  }
}
