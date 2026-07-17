package com.event.EventManage.service;

import com.event.EventManage.dto.ReviewRequest;
import com.event.EventManage.dto.ReviewResponse;
import com.event.EventManage.exception.BadRequestException;
import com.event.EventManage.exception.ResourceNotFoundException;
import com.event.EventManage.model.Item;
import com.event.EventManage.model.Review;
import com.event.EventManage.model.User;
import com.event.EventManage.repository.ItemRepository;
import com.event.EventManage.repository.ReviewRepository;
import com.event.EventManage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    /**
     * Add a review for a decoration item. Each user can only review an item once.
     */
    @Transactional
    public ReviewResponse addReview(String itemId, ReviewRequest request, String userEmail) {
        log.info("User {} adding review for item {}", userEmail, itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Prevent duplicate reviews
        if (reviewRepository.existsByItemIdAndUserId(itemId, user.getId())) {
            throw new BadRequestException("You have already reviewed this item. You can edit your existing review.");
        }

        Review review = Review.builder()
                .item(item)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created with ID: {} for item {}", saved.getId(), itemId);
        return toResponse(saved);
    }

    /**
     * Get all reviews for a specific item.
     */
    public List<ReviewResponse> getReviewsForItem(String itemId) {
        log.info("Fetching reviews for item {}", itemId);
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item not found with id: " + itemId);
        }
        return reviewRepository.findByItemIdOrderByCreatedAtDesc(itemId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get average rating and total count for a specific item.
     */
    public ReviewSummary getItemRatingSummary(String itemId) {
        log.info("Fetching rating summary for item {}", itemId);
        Double avg = reviewRepository.findAverageRatingByItemId(itemId);
        Long count = reviewRepository.countByItemId(itemId);
        return new ReviewSummary(
                itemId,
                avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0,
                count
        );
    }

    /**
     * Update your own review.
     */
    @Transactional
    public ReviewResponse updateReview(String reviewId, ReviewRequest request, String userEmail) {
        log.info("User {} updating review {}", userEmail, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only edit your own reviews.");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review updated = reviewRepository.save(review);
        log.info("Review {} updated successfully", reviewId);
        return toResponse(updated);
    }

    /**
     * Delete a review. Own review or ADMIN can delete.
     */
    @Transactional
    public void deleteReview(String reviewId, String userEmail) {
        log.info("User {} deleting review {}", userEmail, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isOwner = review.getUser().getId().equals(user.getId());

        if (!isOwner && !isAdmin) {
            throw new BadRequestException("You can only delete your own reviews.");
        }

        reviewRepository.delete(review);
        log.info("Review {} deleted", reviewId);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private ReviewResponse toResponse(Review review) {
        String userName = review.getUser().getFirstName() + " " + review.getUser().getLastName();
        return ReviewResponse.builder()
                .id(review.getId())
                .itemId(review.getItem().getId())
                .itemName(review.getItem().getName())
                .userId(review.getUser().getId())
                .userName(userName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    // ─── Inner summary record ──────────────────────────────────────────────────

    public record ReviewSummary(String itemId, Double averageRating, Long totalReviews) {}
}
