package com.event.EventManage.service;
import com.event.EventManage.model.Order;
import com.event.EventManage.model.Booking;
import com.event.EventManage.model.PaymentStatus;
import com.event.EventManage.repository.OrderRepository;
import com.event.EventManage.repository.BookingRepository;
import com.event.EventManage.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final BookingRepository bookingRepository;

    public List<Order> getAllOrders() { 
        log.info("Fetching all orders");
        return orderRepository.findAll(); 
    }
    
    public Order getOrderById(String id) {
        log.info("Fetching order with ID: {}", id);
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
    
    public Order createOrderForBooking(String bookingId, String paymentMethod) {
        log.info("Creating order for booking ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
            
        Order order = Order.builder()
            .user(booking.getUser())
            .booking(booking)
            .totalAmount(booking.getTotalAmount())
            .paymentStatus(PaymentStatus.PAID)
            .paymentMethod(paymentMethod)
            .paymentTransactionId("TXN-" + System.currentTimeMillis())
            .status("COMPLETED")
            .build();
            
        booking.setStatus(com.event.EventManage.model.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }
}
