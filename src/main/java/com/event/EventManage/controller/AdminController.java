package com.event.EventManage.controller;

import com.event.EventManage.dto.AdminStatsDto;
import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getStats() {
        log.info("Received request to get admin stats");
        AdminStatsDto stats = adminService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Admin stats retrieved successfully", HttpStatus.OK.value()));
    }
}
