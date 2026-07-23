package com.event.EventManage.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CartItemRequest {
    private String itemId;
    private Integer quantity;
    private LocalDate eventDate;
    private String selectedPackage;
    private String notes;
}
