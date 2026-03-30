package com.smarthome.service;

import com.smarthome.dto.common.MessageResponse;
import com.smarthome.dto.user.AdminUserDto;
import com.smarthome.entity.User;
import com.smarthome.entity.UserRole;
import com.smarthome.exception.ApiException;
import com.smarthome.repository.PurchaseOrderRepository;
import com.smarthome.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AdminUserService {
  private final UserRepository userRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final PasswordEncoder passwordEncoder;

  public AdminUserService(UserRepository userRepository, PurchaseOrderRepository purchaseOrderRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<AdminUserDto> listUsers() {
    return userRepository.findAll().stream()
      .sorted(Comparator.comparing(User::getId))
      .map(user -> new AdminUserDto(
        user.getId(),
        user.getUsername(),
        user.getRole(),
        purchaseOrderRepository.countByUser_Id(user.getId())
      ))
      .toList();
  }

  public AdminUserDto updateRole(Long userId, UserRole role, String currentUsername) {
    User user = requireUser(userId);
    if (user.getUsername().equals(currentUsername) && role != UserRole.ADMIN) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "CANNOT_DEMOTE_SELF", "You cannot remove your own admin role");
    }

    if (user.getRole() == UserRole.ADMIN && role != UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) <= 1) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "LAST_ADMIN", "System must keep at least one admin");
    }

    user.setRole(role);
    userRepository.save(user);
    return new AdminUserDto(user.getId(), user.getUsername(), user.getRole(), purchaseOrderRepository.countByUser_Id(user.getId()));
  }

  public MessageResponse resetPassword(Long userId, String newPassword) {
    User user = requireUser(userId);
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    return new MessageResponse("Password reset successfully");
  }

  public MessageResponse deleteUser(Long userId, String currentUsername) {
    User user = requireUser(userId);
    if (user.getUsername().equals(currentUsername)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "CANNOT_DELETE_SELF", "You cannot delete the current logged in admin");
    }

    if (user.getRole() == UserRole.ADMIN && userRepository.countByRole(UserRole.ADMIN) <= 1) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "LAST_ADMIN", "System must keep at least one admin");
    }

    if (purchaseOrderRepository.countByUser_Id(userId) > 0) {
      throw new ApiException(HttpStatus.CONFLICT, "USER_HAS_ORDERS", "Cannot delete a user who already has orders");
    }

    userRepository.delete(user);
    return new MessageResponse("User deleted successfully");
  }

  private User requireUser(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
  }
}
