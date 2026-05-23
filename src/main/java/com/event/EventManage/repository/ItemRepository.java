package com.event.EventManage.repository;

import com.event.EventManage.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, String> {
    List<Item> findByCategoryId(String categoryId);
}
