package com.smarthome.service;

import com.smarthome.dto.auth.AuthResponse;
import com.smarthome.dto.auth.ForgotPasswordRequest;
import com.smarthome.dto.auth.LoginRequest;
import com.smarthome.dto.auth.RegisterRequest;
import com.smarthome.dto.common.MessageResponse;
import com.smarthome.entity.User;
import com.smarthome.entity.UserRole;
import com.smarthome.exception.ApiException;
import com.smarthome.repository.UserRepository;
import com.smarthome.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new ApiException(HttpStatus.CONFLICT, "USERNAME_TAKEN", "Username already exists");
    }

    User user = new User();
    user.setUsername(request.getUsername().trim());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole(UserRole.USER);

    userRepository.save(user);

    String token = jwtService.generateToken(user.getUsername());
    return new AuthResponse(token, user.getUsername(), user.getRole());
  }

  public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByUsername(request.getUsername())
      .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid username or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid username or password");
    }

    String token = jwtService.generateToken(user.getUsername());
    return new AuthResponse(token, user.getUsername(), user.getRole());
  }

  public MessageResponse forgotPassword(ForgotPasswordRequest request) {
    User user = userRepository.findByUsername(request.getUsername().trim())
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Username not found"));

    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
    return new MessageResponse("Password updated successfully");
  }
}
