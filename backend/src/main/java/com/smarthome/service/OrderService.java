package com.smarthome.service;

import com.smarthome.config.DemoCatalogDefaults;
import com.smarthome.dto.order.CreateOrderRequest;
import com.smarthome.dto.order.OrderItemRequest;
import com.smarthome.dto.order.PurchaseOrderDto;
import com.smarthome.dto.order.PurchaseOrderItemDto;
import com.smarthome.entity.OrderStatus;
import com.smarthome.entity.Product;
import com.smarthome.entity.PurchaseOrder;
import com.smarthome.entity.PurchaseOrderItem;
import com.smarthome.entity.User;
import com.smarthome.exception.ApiException;
import com.smarthome.repository.ProductRepository;
import com.smarthome.repository.PurchaseOrderRepository;
import com.smarthome.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;

  public OrderService(UserRepository userRepository, ProductRepository productRepository, PurchaseOrderRepository purchaseOrderRepository) {
    this.userRepository = userRepository;
    this.productRepository = productRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
  }

  @Transactional
  public PurchaseOrderDto createOrder(String username, CreateOrderRequest request) {
    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "User not found"));

    PurchaseOrder order = new PurchaseOrder();
    order.setUser(user);
    order.setCustomerName(request.getCustomerName().trim());
    order.setPhoneNumber(request.getPhoneNumber().trim());
    order.setShippingAddress(request.getShippingAddress().trim());
    order.setPaymentMethod(request.getPaymentMethod());
    order.setStatus(OrderStatus.CREATED);

    for (OrderItemRequest itemReq : request.getItems()) {
      Product product = productRepository.findById(itemReq.getProductId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found"));

      int quantity = itemReq.getQuantity();
      if (product.getStock() == null || product.getStock() < quantity) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", "Insufficient stock for product: " + product.getName());
      }

      PurchaseOrderItem item = new PurchaseOrderItem();
      item.setProduct(product);
      item.setQuantity(quantity);
      item.setUnitPrice(product.getPrice());
      item.setUnitCost(DemoCatalogDefaults.resolveCostPrice(product));
      order.addItem(item);

      product.setStock(product.getStock() - quantity);
    }

    return toDto(purchaseOrderRepository.save(order));
  }

  @Transactional(readOnly = true)
  public PurchaseOrderDto getOrderById(String username, Long orderId) {
    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "User not found"));

    PurchaseOrder order = purchaseOrderRepository.findById(orderId)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order not found"));

    if (!order.getUser().getId().equals(user.getId())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You cannot access this order");
    }

    return toDto(order);
  }

  @Transactional(readOnly = true)
  public List<PurchaseOrderDto> listOrders(String username) {
    User user = userRepository.findByUsername(username)
      .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "User not found"));

    return purchaseOrderRepository.findByUser(user).stream()
      .map(this::toDto)
      .toList();
  }

  public PurchaseOrderDto toDto(PurchaseOrder order) {
    List<PurchaseOrderItemDto> itemDtos = order.getItems().stream()
      .map(item -> new PurchaseOrderItemDto(
        item.getProduct().getId(),
        item.getProduct().getName(),
        item.getQuantity(),
        item.getUnitPrice()
      ))
      .toList();

    BigDecimal total = itemDtos.stream()
      .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new PurchaseOrderDto(
      order.getId(),
      order.getStatus(),
      order.getCustomerName(),
      order.getPhoneNumber(),
      order.getShippingAddress(),
      order.getPaymentMethod(),
      order.getCreatedAt(),
      total,
      itemDtos
    );
  }
}
