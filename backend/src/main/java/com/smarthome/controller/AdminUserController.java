package com.smarthome.controller;

import com.smarthome.dto.common.MessageResponse;
import com.smarthome.dto.user.AdminUserDto;
import com.smarthome.dto.user.ResetUserPasswordRequest;
import com.smarthome.dto.user.UpdateUserRoleRequest;
import com.smarthome.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
  private final AdminUserService adminUserService;

  public AdminUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  @GetMapping
  public List<AdminUserDto> list() {
    return adminUserService.listUsers();
  }

  @PutMapping("/{id}/role")
  public AdminUserDto updateRole(@PathVariable("id") Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    return adminUserService.updateRole(id, request.getRole(), currentUsername);
  }

  @PostMapping("/{id}/password")
  public MessageResponse resetPassword(@PathVariable("id") Long id, @Valid @RequestBody ResetUserPasswordRequest request) {
    return adminUserService.resetPassword(id, request.getNewPassword());
  }

  @DeleteMapping("/{id}")
  public MessageResponse delete(@PathVariable("id") Long id) {
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    return adminUserService.deleteUser(id, currentUsername);
  }
}
