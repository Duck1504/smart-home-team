package com.smarthome.controller;

import com.smarthome.dto.stats.AdminStatsDto;
import com.smarthome.service.AdminStatsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {
  private final AdminStatsService adminStatsService;

  public AdminStatsController(AdminStatsService adminStatsService) {
    this.adminStatsService = adminStatsService;
  }

  @GetMapping
  public AdminStatsDto getStats() {
    return adminStatsService.getStats();
  }
}
