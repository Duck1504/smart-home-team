package com.smarthome.dto.user;

import com.smarthome.entity.UserRole;

public class AdminUserDto {
  private final Long id;
  private final String username;
  private final UserRole role;
  private final long orderCount;

  public AdminUserDto(Long id, String username, UserRole role, long orderCount) {
    this.id = id;
    this.username = username;
    this.role = role;
    this.orderCount = orderCount;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public UserRole getRole() {
    return role;
  }

  public long getOrderCount() {
    return orderCount;
  }
}
