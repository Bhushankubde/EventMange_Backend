package com.event.EventManage.controller;
import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.dto.BookingRequest;
import com.event.EventManage.model.Booking;
import com.event.EventManage.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookings() {
        log.info("Received request to get all bookings");
        return ResponseEntity.ok(ApiResponse.success(bookingService.getAllBookings(), "Bookings retrieved successfully", HttpStatus.OK.value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Booking>> getBookingById(@PathVariable String id) {
        log.info("Received request to get booking by ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingById(id), "Booking retrieved successfully", HttpStatus.OK.value()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(@RequestBody BookingRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Received request to create booking for user: {}", userEmail);
        Booking booking = bookingService.createBooking(request, userEmail);
        return new ResponseEntity<>(ApiResponse.success(booking, "Booking created successfully", HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }
}
