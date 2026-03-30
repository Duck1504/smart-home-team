package com.smarthome.dto.stats;

import com.smarthome.entity.OrderStatus;
import com.smarthome.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;

public class RecentOrderDto {
  private final Long id;
  private final String username;
  private final OrderStatus status;
  private final PaymentMethod paymentMethod;
  private final Instant createdAt;
  private final BigDecimal total;
  private final String shippingAddress;

  public RecentOrderDto(
    Long id,
    String username,
    OrderStatus status,
    PaymentMethod paymentMethod,
    Instant createdAt,
    BigDecimal total,
    String shippingAddress
  ) {
    this.id = id;
    this.username = username;
    this.status = status;
    this.paymentMethod = paymentMethod;
    this.createdAt = createdAt;
    this.total = total;
    this.shippingAddress = shippingAddress;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public OrderStatus getStatus() {
    return status;
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

  public String getShippingAddress() {
    return shippingAddress;
  }
}
