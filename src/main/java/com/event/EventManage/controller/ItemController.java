package com.event.EventManage.controller;
import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.model.Item;
import com.event.EventManage.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Item>>> getAllItems(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String search) { 
        log.info("Received request to get items (categoryId: {}, search: {})", categoryId, search);
        return ResponseEntity.ok(ApiResponse.success(itemService.getItems(categoryId, search), "Items retrieved successfully", HttpStatus.OK.value())); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> getItemById(@PathVariable String id) { 
        log.info("Received request to get item by ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(itemService.getItemById(id), "Item retrieved successfully", HttpStatus.OK.value())); 
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Item>> createItem(@RequestBody Item item) { 
        log.info("Received request to create new item: {}", item.getName());
        return new ResponseEntity<>(ApiResponse.success(itemService.createItem(item), "Item created successfully", HttpStatus.CREATED.value()), HttpStatus.CREATED); 
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> updateItem(@PathVariable String id, @RequestBody Item item) { 
        log.info("Received request to update item with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(itemService.updateItem(id, item), "Item updated successfully", HttpStatus.OK.value())); 
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable String id) { 
        log.info("Received request to delete item with ID: {}", id);
        itemService.deleteItem(id); 
        return ResponseEntity.ok(ApiResponse.success(null, "Item deleted successfully", HttpStatus.OK.value())); 
    }
}
