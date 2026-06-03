package com.event.EventManage.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class BookingRequest {
    private List<BookingItemRequest> items;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String eventLocation;
}
