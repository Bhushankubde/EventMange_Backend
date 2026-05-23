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
            Item item = itemRepository.findById(itemReq.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemReq.getItemId()));
            
            if (item.getStock() < itemReq.getQuantity()) {
                log.warn("Insufficient stock for item {} (Requested: {}, Available: {})", item.getName(), itemReq.getQuantity(), item.getStock());
                throw new BadRequestException("Insufficient stock for item: " + item.getName());
            }

            log.info("Reserving {} units of item: {}", itemReq.getQuantity(), item.getName());
            item.setStock(item.getStock() - itemReq.getQuantity());
            itemRepository.save(item);

            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            BookingItem bookingItem = BookingItem.builder()
                    .booking(booking)
                    .item(item)
                    .quantity(itemReq.getQuantity())
                    .price(item.getPrice())
                    .build();
            
            booking.getItems().add(bookingItem);
        }

        booking.setTotalAmount(totalAmount);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with ID: {} and Total Amount: {}", savedBooking.getId(), totalAmount);
        return savedBooking;
    }
}
