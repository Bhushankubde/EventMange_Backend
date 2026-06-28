package com.event.EventManage.service;
import com.event.EventManage.model.Item;
import com.event.EventManage.model.Category;
import com.event.EventManage.repository.ItemRepository;
import com.event.EventManage.repository.CategoryRepository;
import com.event.EventManage.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    
    public List<Item> getAllItems() { 
        log.info("Fetching all items");
        List<Item> items = itemRepository.findAll(); 
        for (Item item : items) {
            if (item.getCategory() != null) {
                item.setTempCategoryId(item.getCategory().getId());
            }
        }
        return items;
    }

    public List<Item> getItems(String categoryId, String search) {
        log.info("Fetching items with categoryId: {}, search: {}", categoryId, search);
        List<Item> items;
        if (categoryId != null && !categoryId.trim().isEmpty() && !categoryId.equalsIgnoreCase("all")) {
            if (search != null && !search.trim().isEmpty()) {
                items = itemRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, search);
            } else {
                items = itemRepository.findByCategoryId(categoryId);
            }
        } else {
            if (search != null && !search.trim().isEmpty()) {
                items = itemRepository.findByNameContainingIgnoreCase(search);
            } else {
                items = itemRepository.findAll();
            }
        }
        
        for (Item item : items) {
            if (item.getCategory() != null) {
                item.setTempCategoryId(item.getCategory().getId());
            }
        }
        
        return items;
    }
    
    public Item getItemById(String id) {
        log.info("Fetching item with ID: {}", id);
        return itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }
    
    public Item createItem(Item item) { 
        log.info("Creating new item: {}", item.getName());
        
        if (item.getAvailable() == null) {
            item.setAvailable(true);
        }
        
        String catId = item.getTempCategoryId();
        if (catId == null && item.getCategory() != null) {
            catId = item.getCategory().getId();
        }
        if (catId != null) {
            Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            item.setCategory(category);
        }
        
        return itemRepository.save(item); 
    }
    
    public Item updateItem(String id, Item updatedItem) {
        log.info("Updating item with ID: {}", id);
        Item item = getItemById(id);
        item.setName(updatedItem.getName());
        item.setDescription(updatedItem.getDescription());
        item.setPrice(updatedItem.getPrice());
        item.setStock(updatedItem.getStock());
        item.setImageUrl(updatedItem.getImageUrl());
        item.setAvailable(updatedItem.getAvailable());
        
        String catId = updatedItem.getTempCategoryId();
        if (catId == null && updatedItem.getCategory() != null) {
            catId = updatedItem.getCategory().getId();
        }
        if (catId != null) {
            Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            item.setCategory(category);
        } else {
            item.setCategory(null);
        }
        
        return itemRepository.save(item);
    }
    
    public void deleteItem(String id) { 
        log.info("Deleting item with ID: {}", id);
        itemRepository.delete(getItemById(id)); 
    }
}
