package com.event.EventManage.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OfflineSaleRequest {
    private String customerId;
    private List<SaleItemRequest> items;
    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal depositAmount;
    private String notes;
}
