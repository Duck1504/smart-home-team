package com.smarthome.service;

import com.smarthome.config.DemoCatalogDefaults;
import com.smarthome.dto.stats.AdminStatsDto;
import com.smarthome.dto.stats.CategorySalesDto;
import com.smarthome.dto.stats.ProductSalesDto;
import com.smarthome.dto.stats.RecentOrderDto;
import com.smarthome.entity.OrderStatus;
import com.smarthome.entity.Product;
import com.smarthome.entity.PurchaseOrder;
import com.smarthome.entity.PurchaseOrderItem;
import com.smarthome.repository.ProductRepository;
import com.smarthome.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminStatsService {
  private final ProductRepository productRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;

  public AdminStatsService(ProductRepository productRepository, PurchaseOrderRepository purchaseOrderRepository) {
    this.productRepository = productRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
  }

  @Transactional(readOnly = true)
  public AdminStatsDto getStats() {
    List<Product> products = productRepository.findAll();
    List<PurchaseOrder> orders = purchaseOrderRepository.findAll();

    Map<Long, MutableProductSales> productSales = new HashMap<>();
    Map<Long, MutableCategorySales> categorySales = new HashMap<>();

    for (Product product : products) {
      productSales.put(product.getId(), new MutableProductSales(product));
      if (product.getCategory() != null) {
        categorySales.putIfAbsent(product.getCategory().getId(), new MutableCategorySales(
          product.getCategory().getId(),
          product.getCategory().getName()
        ));
      }
    }

    long totalOrders = 0;
    long totalUnitsSold = 0;
    BigDecimal totalRevenue = BigDecimal.ZERO;
    BigDecimal totalExpense = BigDecimal.ZERO;
    List<RecentOrderDto> recentOrders = new ArrayList<>();

    for (PurchaseOrder order : orders.stream().sorted(Comparator.comparing(PurchaseOrder::getCreatedAt).reversed()).toList()) {
      if (order.getStatus() == OrderStatus.CANCELLED) {
        continue;
      }

      totalOrders++;
      BigDecimal orderTotal = BigDecimal.ZERO;

      for (PurchaseOrderItem item : order.getItems()) {
        MutableProductSales productStat = productSales.get(item.getProduct().getId());
        BigDecimal unitCost = item.getUnitCost() != null
          ? item.getUnitCost()
          : DemoCatalogDefaults.resolveCostPrice(item.getProduct());

        if (productStat != null) {
          productStat.add(item.getQuantity(), item.getUnitPrice(), unitCost);
        }

        MutableCategorySales categoryStat = categorySales.get(item.getProduct().getCategory().getId());
        if (categoryStat != null) {
          categoryStat.add(item.getQuantity(), item.getUnitPrice(), unitCost);
        }

        BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        BigDecimal lineExpense = unitCost.multiply(BigDecimal.valueOf(item.getQuantity()));
        totalUnitsSold += item.getQuantity();
        totalRevenue = totalRevenue.add(lineTotal);
        totalExpense = totalExpense.add(lineExpense);
        orderTotal = orderTotal.add(lineTotal);
      }

      if (recentOrders.size() < 10) {
        recentOrders.add(new RecentOrderDto(
          order.getId(),
          order.getUser().getUsername(),
          order.getStatus(),
          order.getPaymentMethod(),
          order.getCreatedAt(),
          orderTotal,
          order.getShippingAddress()
        ));
      }
    }

    List<ProductSalesDto> productStats = productSales.values().stream()
      .sorted(Comparator.comparingLong(MutableProductSales::getSoldQuantity).reversed()
        .thenComparing(MutableProductSales::getProductName))
      .map(MutableProductSales::toDto)
      .toList();

    List<CategorySalesDto> categoryStats = categorySales.values().stream()
      .sorted(Comparator.comparingLong(MutableCategorySales::getSoldQuantity).reversed()
        .thenComparing(MutableCategorySales::getCategoryName))
      .map(MutableCategorySales::toDto)
      .toList();

    long lowStockProducts = products.stream()
      .filter(product -> product.getStock() != null && product.getStock() <= 5)
      .count();

    return new AdminStatsDto(
      totalOrders,
      totalUnitsSold,
      totalRevenue,
      totalExpense,
      totalRevenue.subtract(totalExpense),
      products.size(),
      lowStockProducts,
      productStats,
      categoryStats,
      recentOrders
    );
  }

  private static class MutableProductSales {
    private final Product product;
    private long soldQuantity;
    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal expense = BigDecimal.ZERO;

    private MutableProductSales(Product product) {
      this.product = product;
    }

    private void add(int quantity, BigDecimal unitPrice, BigDecimal unitCost) {
      soldQuantity += quantity;
      revenue = revenue.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
      expense = expense.add(unitCost.multiply(BigDecimal.valueOf(quantity)));
    }

    private long getSoldQuantity() {
      return soldQuantity;
    }

    private String getProductName() {
      return product.getName();
    }

    private ProductSalesDto toDto() {
      return new ProductSalesDto(
        product.getId(),
        product.getName(),
        product.getCategory() == null ? "" : product.getCategory().getName(),
        product.getStock(),
        soldQuantity,
        revenue,
        expense,
        revenue.subtract(expense)
      );
    }
  }

  private static class MutableCategorySales {
    private final Long categoryId;
    private final String categoryName;
    private long soldQuantity;
    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal expense = BigDecimal.ZERO;

    private MutableCategorySales(Long categoryId, String categoryName) {
      this.categoryId = categoryId;
      this.categoryName = categoryName;
    }

    private void add(int quantity, BigDecimal unitPrice, BigDecimal unitCost) {
      soldQuantity += quantity;
      revenue = revenue.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
      expense = expense.add(unitCost.multiply(BigDecimal.valueOf(quantity)));
    }

    private long getSoldQuantity() {
      return soldQuantity;
    }

    private String getCategoryName() {
      return categoryName;
    }

    private CategorySalesDto toDto() {
      return new CategorySalesDto(categoryId, categoryName, soldQuantity, revenue, expense, revenue.subtract(expense));
    }
  }
}
