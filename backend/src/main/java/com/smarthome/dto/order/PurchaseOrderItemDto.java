package com.smarthome.dto.order;

import java.math.BigDecimal;

public class PurchaseOrderItemDto {
  private Long productId;
  private String productName;
  private Integer quantity;
  private BigDecimal unitPrice;

  public PurchaseOrderItemDto(Long productId, String productName, Integer quantity, BigDecimal unitPrice) {
    this.productId = productId;
    this.productName = productName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
  }

  public Long getProductId() {
    return productId;
  }

  public String getProductName() {
    return productName;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }
}

