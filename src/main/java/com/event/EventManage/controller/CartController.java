package com.event.EventManage.controller;

import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.dto.CartItemRequest;
import com.event.EventManage.model.CartItem;
import com.event.EventManage.service.CartItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItem>>> getCart(Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Received request to get cart for user: {}", userEmail);
        List<CartItem> cart = cartItemService.getCartItems(userEmail);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart retrieved successfully", HttpStatus.OK.value()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartItem>> addToCart(@RequestBody CartItemRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Received request to add item {} to cart for user: {}", request.getItemId(), userEmail);
        CartItem cartItem = cartItemService.addToCart(request, userEmail);
        return new ResponseEntity<>(ApiResponse.success(cartItem, "Item added to cart successfully", HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CartItem>> updateQuantity(
            @PathVariable String id,
            @RequestBody Map<String, Integer> body,
            Authentication authentication) {
        String userEmail = authentication.getName();
        if (!body.containsKey("quantity")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Quantity is required", HttpStatus.BAD_REQUEST.value()));
        }
        int quantity = body.get("quantity");
        log.info("Received request to update quantity of cart item {} to {} for user: {}", id, quantity, userEmail);
        CartItem cartItem = cartItemService.updateQuantity(id, quantity, userEmail);
        return ResponseEntity.ok(ApiResponse.success(cartItem, "Cart item quantity updated successfully", HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@PathVariable String id, Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Received request to remove cart item {} for user: {}", id, userEmail);
        cartItemService.removeFromCart(id, userEmail);
        return ResponseEntity.ok(ApiResponse.success(null, "Item removed from cart successfully", HttpStatus.OK.value()));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Received request to clear cart for user: {}", userEmail);
        cartItemService.clearCart(userEmail);
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared successfully", HttpStatus.OK.value()));
    }
}
