package com.event.EventManage.service;

import com.event.EventManage.dto.CartItemRequest;
import com.event.EventManage.exception.BadRequestException;
import com.event.EventManage.exception.ResourceNotFoundException;
import com.event.EventManage.model.CartItem;
import com.event.EventManage.model.Item;
import com.event.EventManage.model.User;
import com.event.EventManage.repository.CartItemRepository;
import com.event.EventManage.repository.ItemRepository;
import com.event.EventManage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemService {
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public List<CartItem> getCartItems(String userEmail) {
        log.info("Fetching cart items for user email: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return cartItemRepository.findByUserId(user.getId());
    }

    @Transactional
    public CartItem addToCart(CartItemRequest request, String userEmail) {
        log.info("Adding item {} to cart for user {}", request.getItemId(), userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be at least 1");
        }
        if (request.getEventDate() == null) {
            throw new BadRequestException("Event date is required");
        }

        BigDecimal calculatedPrice = calculateUnitPrice(item, request.getSelectedPackage());

        Optional<CartItem> existingOpt = cartItemRepository.findByUserIdAndItemIdAndEventDateAndSelectedPackage(
                user.getId(), item.getId(), request.getEventDate(), request.getSelectedPackage());

        if (existingOpt.isPresent()) {
            CartItem existing = existingOpt.get();
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            // Update notes if provided
            if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
                existing.setNotes(request.getNotes());
            }
            log.info("Incrementing quantity for existing cart item ID: {}", existing.getId());
            return cartItemRepository.save(existing);
        } else {
            CartItem cartItem = CartItem.builder()
                    .user(user)
                    .item(item)
                    .eventDate(request.getEventDate())
                    .quantity(request.getQuantity())
                    .selectedPackage(request.getSelectedPackage())
                    .notes(request.getNotes())
                    .price(calculatedPrice)
                    .build();
            log.info("Creating new cart item");
            return cartItemRepository.save(cartItem);
        }
    }

    @Transactional
    public CartItem updateQuantity(String cartItemId, int quantity, String userEmail) {
        log.info("Updating quantity for cart item {} to {} for user {}", cartItemId, quantity, userEmail);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("Access denied: You do not own this cart item");
        }

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(String cartItemId, String userEmail) {
        log.info("Removing cart item {} for user {}", cartItemId, userEmail);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("Access denied: You do not own this cart item");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(String userEmail) {
        log.info("Clearing cart for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<CartItem> items = cartItemRepository.findByUserId(user.getId());
        cartItemRepository.deleteAll(items);
    }

    private BigDecimal calculateUnitPrice(Item item, String selectedPackage) {
        if (selectedPackage == null) {
            return item.getPrice();
        }
        String pkg = selectedPackage.toLowerCase();
        BigDecimal multiplier = BigDecimal.ONE;
        if (pkg.contains("premium") || pkg.contains("deluxe")) {
            multiplier = new BigDecimal("1.75");
        } else if (pkg.contains("standard")) {
            multiplier = new BigDecimal("1.35");
        }
        return item.getPrice().multiply(multiplier);
    }
}
