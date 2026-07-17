package com.event.EventManage.controller;

import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class AvailabilityController {

    private final ItemService itemService;

    /**
     * Check if an item has sufficient available stock for a given date range.
     * <p>
     * GET /api/items/{itemId}/availability?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD&quantity=N
     * <p>
     * This considers all non-cancelled bookings that overlap with the requested range
     * to determine real-time availability.
     */
    @GetMapping("/{itemId}/availability")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkAvailability(
            @PathVariable String itemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int quantity) {

        // Default endDate to startDate for single-day events
        LocalDate effectiveEndDate = (endDate != null) ? endDate : startDate;

        if (effectiveEndDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "endDate must be on or after startDate", HttpStatus.BAD_REQUEST.value()));
        }
        if (quantity <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "quantity must be at least 1", HttpStatus.BAD_REQUEST.value()));
        }

        log.info("Checking availability: item={}, from={}, to={}, qty={}", itemId, startDate, effectiveEndDate, quantity);

        boolean available = itemService.checkAvailability(itemId, quantity, startDate, effectiveEndDate);

        Map<String, Object> result = Map.of(
                "itemId", itemId,
                "startDate", startDate.toString(),
                "endDate", effectiveEndDate.toString(),
                "requestedQuantity", quantity,
                "available", available
        );

        String message = available
                ? "Item is available for the requested dates and quantity."
                : "Item is NOT available for the requested dates and quantity.";

        return ResponseEntity.ok(ApiResponse.success(result, message, HttpStatus.OK.value()));
    }
}
