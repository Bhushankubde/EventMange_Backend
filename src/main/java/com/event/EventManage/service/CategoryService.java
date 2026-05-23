package com.event.EventManage.service;
import com.event.EventManage.model.Category;
import com.event.EventManage.repository.CategoryRepository;
import com.event.EventManage.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    
    public List<Category> getAllCategories() { 
        log.info("Fetching all categories");
        return categoryRepository.findAll(); 
    }
    
    public Category getCategoryById(String id) {
        log.info("Fetching category with ID: {}", id);
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
    
    public Category createCategory(Category category) { 
        log.info("Creating new category: {}", category.getName());
        return categoryRepository.save(category); 
    }
    
    public Category updateCategory(String id, Category updatedCategory) {
        log.info("Updating category with ID: {}", id);
        Category category = getCategoryById(id);
        category.setName(updatedCategory.getName());
        category.setDescription(updatedCategory.getDescription());
        category.setImageUrl(updatedCategory.getImageUrl());
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(String id) {
        log.info("Deleting category with ID: {}", id);
        categoryRepository.delete(getCategoryById(id));
    }
}
