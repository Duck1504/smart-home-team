package com.smarthome.dto.user;

import com.smarthome.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public class UpdateUserRoleRequest {
  @NotNull
  private UserRole role;

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }
}
