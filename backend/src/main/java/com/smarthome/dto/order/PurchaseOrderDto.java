package com.smarthome.dto.order;

import com.smarthome.entity.PaymentMethod;
import com.smarthome.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class PurchaseOrderDto {
  private Long id;
  private OrderStatus status;
  private String customerName;
  private String phoneNumber;
  private String shippingAddress;
  private PaymentMethod paymentMethod;
  private Instant createdAt;
  private BigDecimal total;
  private List<PurchaseOrderItemDto> items;

  public PurchaseOrderDto(
    Long id,
    OrderStatus status,
    String customerName,
    String phoneNumber,
    String shippingAddress,
    PaymentMethod paymentMethod,
    Instant createdAt,
    BigDecimal total,
    List<PurchaseOrderItemDto> items
  ) {
    this.id = id;
    this.status = status;
    this.customerName = customerName;
    this.phoneNumber = phoneNumber;
    this.shippingAddress = shippingAddress;
    this.paymentMethod = paymentMethod;
    this.createdAt = createdAt;
    this.total = total;
    this.items = items;
  }

  public Long getId() {
    return id;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public String getCustomerName() {
    return customerName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getShippingAddress() {
    return shippingAddress;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public List<PurchaseOrderItemDto> getItems() {
    return items;
  }
}

