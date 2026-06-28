package com.event.EventManage.service;

import com.event.EventManage.dto.AdminStatsDto;
import com.event.EventManage.model.Role;
import com.event.EventManage.repository.BookingRepository;
import com.event.EventManage.repository.ItemRepository;
import com.event.EventManage.repository.OfflineSaleRepository;
import com.event.EventManage.repository.OrderRepository;
import com.event.EventManage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final OrderRepository orderRepository;
    private final OfflineSaleRepository offlineSaleRepository;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public AdminStatsDto getStats() {
        log.info("Fetching admin dashboard stats");

        BigDecimal onlineRevenue = orderRepository.sumTotalAmount();
        BigDecimal offlineRevenue = offlineSaleRepository.sumTotalAmount();
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (onlineRevenue != null) {
            totalRevenue = totalRevenue.add(onlineRevenue);
        }
        if (offlineRevenue != null) {
            totalRevenue = totalRevenue.add(offlineRevenue);
        }

        long totalBookings = bookingRepository.count();
        long activeItems = itemRepository.countByAvailable(true);
        long newCustomers = userRepository.countByRole(Role.CUSTOMER);

        return AdminStatsDto.builder()
                .totalRevenue(totalRevenue)
                .totalBookings(totalBookings)
                .activeItems(activeItems)
                .newCustomers(newCustomers)
                .build();
    }
}
