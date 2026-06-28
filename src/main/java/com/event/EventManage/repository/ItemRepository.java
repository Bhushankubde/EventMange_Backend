package com.event.EventManage.repository;

import com.event.EventManage.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, String> {
    @Query("SELECT i FROM Item i WHERE i.category.id = :categoryId")
    List<Item> findByCategoryId(@Param("categoryId") String categoryId);

    @Query("SELECT i FROM Item i WHERE i.category.id = :categoryId AND LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Item> findByCategoryIdAndNameContainingIgnoreCase(@Param("categoryId") String categoryId, @Param("name") String name);

    List<Item> findByNameContainingIgnoreCase(String name);

    long countByAvailable(boolean available);
}
