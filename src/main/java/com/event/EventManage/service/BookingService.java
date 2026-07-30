package com.event.EventManage.service;
import com.event.EventManage.dto.BookingRequest;
import com.event.EventManage.dto.BookingItemRequest;
import com.event.EventManage.model.*;
import com.event.EventManage.repository.BookingRepository;
import com.event.EventManage.repository.UserRepository;
import com.event.EventManage.repository.ItemRepository;
import com.event.EventManage.exception.ResourceNotFoundException;
import com.event.EventManage.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;
    private final NotificationService notificationService;

    public List<Booking> getAllBookings() { 
        log.info("Fetching all bookings from database");
        return bookingRepository.findAll(); 
    }
    
    public Booking getBookingById(String id) {
        log.info("Fetching booking with ID: {}", id);
        return bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }
    
    public List<Booking> getUserBookings(String userId) {
        log.info("Fetching bookings for user ID: {}", userId);
        return bookingRepository.findByUserId(userId);
    }
    
    @Transactional
    public Booking createBooking(BookingRequest request, String userEmail) {
        log.info("Creating new booking for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Booking booking = Booking.builder()
                .user(user)
                .eventDate(request.getEventDate())
                .eventTime(request.getEventTime())
                .eventLocation(request.getEventLocation())
                .status(BookingStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (BookingItemRequest itemReq : request.getItems()) {
            Item item = itemRepository.findByIdForUpdate(itemReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemReq.getItemId()));

            int available = item.getAvailableQuantity() != null ? item.getAvailableQuantity() : 0;
            int requested = itemReq.getQuantity();

            if (available <= 0) {
                throw new BadRequestException("Item '" + item.getName() + "' is out of stock.");
            }
            if (available < requested) {
                throw new BadRequestException("Only " + available + " units are available for '" + item.getName() + "'.");
            }

            log.info("Reserving {} units of item: {}", requested, item.getName());
            item.setAvailableQuantity(available - requested);
            item.setStock(available - requested);
            itemRepository.save(item);
            
            try {
                notificationService.broadcastInventoryUpdate(item.getId(), item.getAvailableQuantity());
            } catch (Exception e) {
                log.error("Failed to broadcast inventory update", e);
            }

            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(requested));
            totalAmount = totalAmount.add(itemTotal);

            BookingItem bookingItem = BookingItem.builder()
                    .booking(booking)
                    .item(item)
                    .quantity(requested)
                    .price(item.getPrice())
                    .build();
            
            booking.getItems().add(bookingItem);
        }

        booking.setTotalAmount(totalAmount);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with ID: {} and Total Amount: {}", savedBooking.getId(), totalAmount);
        try {
            notificationService.sendNotification(
                "New Online Booking created: ID #" + savedBooking.getId().substring(0, 6).toUpperCase() + 
                " by " + user.getFirstName() + " " + user.getLastName() + " (Total: ₹" + totalAmount + ")",
                "INFO"
            );
        } catch (Exception e) {
            log.error("Failed to send online booking notification", e);
        }
        return savedBooking;
    }

    @Transactional
    public Booking updateBookingStatus(String id, String status) {
        log.info("Updating status of booking ID: {} to {}", id, status);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        BookingStatus newStatus = BookingStatus.valueOf(status.toUpperCase());
        BookingStatus oldStatus = booking.getStatus();
        
        if (newStatus != oldStatus) {
            booking.setStatus(newStatus);
            
            boolean isNewRestocked = newStatus == BookingStatus.CANCELLED || newStatus == BookingStatus.COMPLETED;
            boolean isOldRestocked = oldStatus == BookingStatus.CANCELLED || oldStatus == BookingStatus.COMPLETED;
            
            if (isNewRestocked && !isOldRestocked) {
                // Restock items
                for (BookingItem bookingItem : booking.getItems()) {
                    Item item = itemRepository.findByIdForUpdate(bookingItem.getItem().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                    int currentAvailable = item.getAvailableQuantity() != null ? item.getAvailableQuantity() : 0;
                    item.setAvailableQuantity(currentAvailable + bookingItem.getQuantity());
                    item.setStock(currentAvailable + bookingItem.getQuantity());
                    itemRepository.save(item);
                    
                    try {
                        notificationService.broadcastInventoryUpdate(item.getId(), item.getAvailableQuantity());
                    } catch (Exception e) {
                        log.error("Failed to broadcast inventory update", e);
                    }
                }
            } else if (!isNewRestocked && isOldRestocked) {
                // Deduct items (Re-opening booking)
                for (BookingItem bookingItem : booking.getItems()) {
                    Item item = itemRepository.findByIdForUpdate(bookingItem.getItem().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                    int currentAvailable = item.getAvailableQuantity() != null ? item.getAvailableQuantity() : 0;
                    int requested = bookingItem.getQuantity();
                    
                    if (currentAvailable < requested) {
                        throw new BadRequestException("Cannot reopen booking: Item '" + item.getName() + "' does not have enough available stock.");
                    }
                    
                    item.setAvailableQuantity(currentAvailable - requested);
                    item.setStock(currentAvailable - requested);
                    itemRepository.save(item);
                    
                    try {
                        notificationService.broadcastInventoryUpdate(item.getId(), item.getAvailableQuantity());
                    } catch (Exception e) {
                        log.error("Failed to broadcast inventory update", e);
                    }
                }
            }
        }
        
        return bookingRepository.save(booking);
    }
}
