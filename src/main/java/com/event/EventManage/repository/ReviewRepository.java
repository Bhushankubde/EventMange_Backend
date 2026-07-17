package com.event.EventManage.repository;

import com.event.EventManage.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {

    List<Review> findByItemIdOrderByCreatedAtDesc(String itemId);

    List<Review> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Review> findByItemIdAndUserId(String itemId, String userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.item.id = :itemId")
    Double findAverageRatingByItemId(@Param("itemId") String itemId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.item.id = :itemId")
    Long countByItemId(@Param("itemId") String itemId);

    boolean existsByItemIdAndUserId(String itemId, String userId);
}
