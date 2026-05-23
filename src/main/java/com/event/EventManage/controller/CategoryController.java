package com.event.EventManage.controller;
import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.model.Category;
import com.event.EventManage.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() { 
        log.info("Received request to get all categories");
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories(), "Categories retrieved successfully", HttpStatus.OK.value())); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable String id) { 
        log.info("Received request to get category by ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryById(id), "Category retrieved successfully", HttpStatus.OK.value())); 
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody Category category) { 
        log.info("Received request to create new category: {}", category.getName());
        return new ResponseEntity<>(ApiResponse.success(categoryService.createCategory(category), "Category created successfully", HttpStatus.CREATED.value()), HttpStatus.CREATED); 
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable String id, @RequestBody Category category) { 
        log.info("Received request to update category with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(id, category), "Category updated successfully", HttpStatus.OK.value())); 
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) { 
        log.info("Received request to delete category with ID: {}", id);
        categoryService.deleteCategory(id); 
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully", HttpStatus.OK.value())); 
    }
}
