package com.smarthome.dto.order;

import com.smarthome.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateOrderRequest {
  @NotBlank(message = "Vui long nhap ho va ten nguoi nhan")
  @Size(max = 120, message = "Ho va ten khong duoc vuot qua 120 ky tu")
  private String customerName;

  @NotBlank(message = "Vui long nhap so dien thoai")
  @Pattern(regexp = "^[0-9+][0-9\\s.-]{7,19}$", message = "So dien thoai khong hop le")
  private String phoneNumber;

  @NotBlank(message = "Vui long nhap dia chi nhan hang")
  @Size(max = 500, message = "Dia chi nhan hang khong duoc vuot qua 500 ky tu")
  private String shippingAddress;

  @NotNull(message = "Vui long chon phuong thuc thanh toan")
  private PaymentMethod paymentMethod;

  @NotEmpty(message = "Gio hang dang trong")
  @Valid
  private List<OrderItemRequest> items;

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getShippingAddress() {
    return shippingAddress;
  }

  public void setShippingAddress(String shippingAddress) {
    this.shippingAddress = shippingAddress;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public List<OrderItemRequest> getItems() {
    return items;
  }

  public void setItems(List<OrderItemRequest> items) {
    this.items = items;
  }
}

