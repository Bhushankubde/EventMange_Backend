package com.event.EventManage.controller;
import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.dto.OfflineSaleRequest;
import com.event.EventManage.model.OfflineSale;
import com.event.EventManage.service.OfflineSaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/offline-sales")
@RequiredArgsConstructor
public class OfflineSaleController {
    private final OfflineSaleService saleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OfflineSale>>> getAll() { 
        log.info("Received request to get all offline sales");
        return ResponseEntity.ok(ApiResponse.success(saleService.getAll(), "Offline sales retrieved successfully", HttpStatus.OK.value())); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OfflineSale>> getById(@PathVariable String id) { 
        log.info("Received request to get offline sale by ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(saleService.getById(id), "Offline sale retrieved successfully", HttpStatus.OK.value())); 
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OfflineSale>> createSale(@RequestBody OfflineSaleRequest request, Authentication auth) {
        String staffEmail = auth.getName();
        log.info("Received request to create new offline sale recorded by: {}", staffEmail);
        OfflineSale sale = saleService.createSale(request, staffEmail);
        return new ResponseEntity<>(ApiResponse.success(sale, "Offline sale created successfully", HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }
}
