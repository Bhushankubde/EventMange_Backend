package com.event.EventManage.service;
import com.event.EventManage.dto.OfflineSaleRequest;
import com.event.EventManage.dto.SaleItemRequest;
import com.event.EventManage.model.*;
import com.event.EventManage.repository.OfflineSaleRepository;
import com.event.EventManage.repository.WalkInCustomerRepository;
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
public class OfflineSaleService {
    private final OfflineSaleRepository offlineSaleRepository;
    private final WalkInCustomerRepository customerRepository;
    private final ItemRepository itemRepository;

    public List<OfflineSale> getAll() { 
        log.info("Fetching all offline sales");
        return offlineSaleRepository.findAll(); 
    }
    
    public OfflineSale getById(String id) { 
        log.info("Fetching offline sale with ID: {}", id);
        return offlineSaleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale not found")); 
    }

    @Transactional
    public OfflineSale createSale(OfflineSaleRequest request, String staffEmail) {
        log.info("Creating new offline sale recorded by: {}", staffEmail);
        WalkInCustomer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("WalkInCustomer not found"));

        OfflineSale sale = OfflineSale.builder()
                .customer(customer)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.valueOf(request.getPaymentStatus().toUpperCase()))
                .rentalStartDate(request.getRentalStartDate())
                .rentalEndDate(request.getRentalEndDate())
                .depositAmount(request.getDepositAmount())
                .notes(request.getNotes())
                .recordedBy(staffEmail)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SaleItemRequest itemReq : request.getItems()) {
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

            OfflineSaleItem saleItem = OfflineSaleItem.builder()
                    .sale(sale)
                    .item(item)
                    .quantity(itemReq.getQuantity())
                    .price(item.getPrice())
                    .build();

            sale.getItems().add(saleItem);
        }

        sale.setTotalAmount(totalAmount);
        OfflineSale savedSale = offlineSaleRepository.save(sale);
        log.info("Offline sale created successfully with ID: {} and Total Amount: {}", savedSale.getId(), totalAmount);
        return savedSale;
    }
}
