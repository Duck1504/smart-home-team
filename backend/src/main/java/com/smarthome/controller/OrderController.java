package com.smarthome.controller;

import com.smarthome.dto.order.CreateOrderRequest;
import com.smarthome.dto.order.PurchaseOrderDto;
import com.smarthome.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public PurchaseOrderDto create(@Valid @RequestBody CreateOrderRequest request) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return orderService.createOrder(username, request);
  }

  @GetMapping
  public List<PurchaseOrderDto> list() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return orderService.listOrders(username);
  }

  @GetMapping("/{id}")
  public PurchaseOrderDto getById(@PathVariable("id") Long orderId) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return orderService.getOrderById(username, orderId);
  }
}

