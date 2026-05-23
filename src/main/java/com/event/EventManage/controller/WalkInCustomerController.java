package com.event.EventManage.controller;
import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.model.WalkInCustomer;
import com.event.EventManage.service.WalkInCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/walk-in-customers")
@RequiredArgsConstructor
public class WalkInCustomerController {
    private final WalkInCustomerService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WalkInCustomer>>> getAll() { 
        log.info("Received request to get all walk-in customers");
        return ResponseEntity.ok(ApiResponse.success(service.getAll(), "Customers retrieved successfully", HttpStatus.OK.value())); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WalkInCustomer>> getById(@PathVariable String id) { 
        log.info("Received request to get walk-in customer by ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(service.getById(id), "Customer retrieved successfully", HttpStatus.OK.value())); 
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WalkInCustomer>> create(@RequestBody WalkInCustomer customer) { 
        log.info("Received request to create new walk-in customer: {} {}", customer.getFirstName(), customer.getLastName());
        return new ResponseEntity<>(ApiResponse.success(service.create(customer), "Customer created successfully", HttpStatus.CREATED.value()), HttpStatus.CREATED); 
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<WalkInCustomer>> search(@RequestParam String phone) { 
        log.info("Received request to search walk-in customer by phone: {}", phone);
        return ResponseEntity.ok(ApiResponse.success(service.searchByPhone(phone), "Customer found successfully", HttpStatus.OK.value())); 
    }
}
