package com.event.EventManage.repository;

import com.event.EventManage.model.Booking;
import com.event.EventManage.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findByUserId(String userId);

    List<Booking> findByStatus(BookingStatus status);

    /**
     * Returns the total quantity of an item already reserved in bookings
     * that overlap with the given date range, excluding CANCELLED bookings.
     *
     * A booking overlaps with [startDate, endDate] if its eventDate falls within the range.
     */
    @Query("""
        SELECT COALESCE(SUM(bi.quantity), 0)
        FROM BookingItem bi
        JOIN bi.booking b
        WHERE bi.item.id = :itemId
          AND b.status <> com.event.EventManage.model.BookingStatus.CANCELLED
          AND b.eventDate BETWEEN :startDate AND :endDate
        """)
    Long findReservedQuantityForItemInDateRange(
            @Param("itemId") String itemId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

