package com.smarthome.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handle(ApiException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("code", ex.getCode());
    body.put("message", ex.getMessage());
    return new ResponseEntity<>(body, ex.getStatus());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("code", "VALIDATION_ERROR");
    String message = ex.getBindingResult().getFieldErrors().stream()
      .findFirst()
      .map(error -> error.getField() + ": " + error.getDefaultMessage())
      .orElse("Request validation failed");
    body.put("message", message);
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("code", "FORBIDDEN");
    body.put("message", "You do not have permission to access this resource");
    return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("code", "DATA_INTEGRITY_ERROR");
    body.put("message", "Operation could not be completed because related data still exists");
    return new ResponseEntity<>(body, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("code", "INTERNAL_ERROR");
    body.put("message", ex.getMessage());
    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}

