package com.event.EventManage.service;
import com.event.EventManage.model.Item;
import com.event.EventManage.repository.ItemRepository;
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
    
    public List<Item> getAllItems() { 
        log.info("Fetching all items");
        return itemRepository.findAll(); 
    }
    
    public Item getItemById(String id) {
        log.info("Fetching item with ID: {}", id);
        return itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }
    
    public Item createItem(Item item) { 
        log.info("Creating new item: {}", item.getName());
        return itemRepository.save(item); 
    }
    
    public Item updateItem(String id, Item updatedItem) {
        log.info("Updating item with ID: {}", id);
        Item item = getItemById(id);
        item.setName(updatedItem.getName());
        item.setDescription(updatedItem.getDescription());
        item.setPrice(updatedItem.getPrice());
        item.setStock(updatedItem.getStock());
        return itemRepository.save(item);
    }
    
    public void deleteItem(String id) { 
        log.info("Deleting item with ID: {}", id);
        itemRepository.delete(getItemById(id)); 
    }
}
