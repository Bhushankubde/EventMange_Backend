package com.event.EventManage.repository;

import com.event.EventManage.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByUserId(String userId);

    Optional<CartItem> findByUserIdAndItemIdAndEventDateAndSelectedPackage(
            String userId, String itemId, LocalDate eventDate, String selectedPackage);
}
