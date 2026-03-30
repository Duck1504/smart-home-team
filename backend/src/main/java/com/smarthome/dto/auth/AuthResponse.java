package com.smarthome.dto.auth;

import com.smarthome.entity.UserRole;

public class AuthResponse {
  private String token;
  private String username;
  private UserRole role;

  public AuthResponse(String token, String username, UserRole role) {
    this.token = token;
    this.username = username;
    this.role = role;
  }

  public String getToken() {
    return token;
  }

  public String getUsername() {
    return username;
  }

  public UserRole getRole() {
    return role;
  }
}

