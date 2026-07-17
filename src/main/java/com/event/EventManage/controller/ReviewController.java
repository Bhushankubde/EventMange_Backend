package com.event.EventManage.controller;

import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.dto.ReviewRequest;
import com.event.EventManage.dto.ReviewResponse;
import com.event.EventManage.service.ReviewService;
import com.event.EventManage.service.ReviewService.ReviewSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * GET /api/items/{itemId}/reviews — Public: list all reviews for an item.
     */
    @GetMapping("/api/items/{itemId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @PathVariable String itemId) {
        log.info("Fetching reviews for item: {}", itemId);
        List<ReviewResponse> reviews = reviewService.getReviewsForItem(itemId);
        return ResponseEntity.ok(ApiResponse.success(reviews,
                "Reviews retrieved successfully", HttpStatus.OK.value()));
    }

    /**
     * GET /api/items/{itemId}/reviews/summary — Public: average rating + total count.
     */
    @GetMapping("/api/items/{itemId}/reviews/summary")
    public ResponseEntity<ApiResponse<ReviewSummary>> getRatingSummary(
            @PathVariable String itemId) {
        log.info("Fetching rating summary for item: {}", itemId);
        ReviewSummary summary = reviewService.getItemRatingSummary(itemId);
        return ResponseEntity.ok(ApiResponse.success(summary,
                "Rating summary retrieved", HttpStatus.OK.value()));
    }

    /**
     * POST /api/items/{itemId}/reviews — Auth: add a review (one per user per item).
     */
    @PostMapping("/api/items/{itemId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @PathVariable String itemId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("User {} submitting review for item: {}", userDetails.getUsername(), itemId);
        ReviewResponse review = reviewService.addReview(itemId, request, userDetails.getUsername());
        return new ResponseEntity<>(ApiResponse.success(review,
                "Review submitted successfully", HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    /**
     * PUT /api/reviews/{reviewId} — Auth: update your own review.
     */
    @PutMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("User {} updating review: {}", userDetails.getUsername(), reviewId);
        ReviewResponse updated = reviewService.updateReview(reviewId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(updated,
                "Review updated successfully", HttpStatus.OK.value()));
    }

    /**
     * DELETE /api/reviews/{reviewId} — Auth: delete your own review (or ADMIN).
     */
    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable String reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("User {} deleting review: {}", userDetails.getUsername(), reviewId);
        reviewService.deleteReview(reviewId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null,
                "Review deleted successfully", HttpStatus.OK.value()));
    }
}
