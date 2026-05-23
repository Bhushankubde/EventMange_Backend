package com.event.EventManage.controller;
import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.model.Order;
import com.event.EventManage.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() { 
        log.info("Received request to get all orders");
        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders(), "Orders retrieved successfully", HttpStatus.OK.value())); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable String id) { 
        log.info("Received request to get order by ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id), "Order retrieved successfully", HttpStatus.OK.value())); 
    }

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<Order>> createOrder(@PathVariable String bookingId, @RequestParam String paymentMethod) {
        log.info("Received request to create order for booking ID: {} with payment method: {}", bookingId, paymentMethod);
        Order order = orderService.createOrderForBooking(bookingId, paymentMethod);
        return new ResponseEntity<>(ApiResponse.success(order, "Order created successfully", HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }
}
