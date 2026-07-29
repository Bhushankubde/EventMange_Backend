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

        BigDecimal paid = request.getPaidAmount() != null ? request.getPaidAmount() : totalAmount;
        BigDecimal pending = totalAmount.subtract(paid);
        sale.setPaidAmount(paid);
        sale.setPendingAmount(pending.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pending);

        if (paid.compareTo(BigDecimal.ZERO) == 0) {
            sale.setPaymentStatus(PaymentStatus.PENDING);
        } else if (sale.getPendingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            sale.setPaymentStatus(PaymentStatus.PAID);
        } else {
            sale.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        sale.setRentalStatus("ACTIVE");

        OfflineSale savedSale = offlineSaleRepository.save(sale);
        log.info("Offline sale created successfully with ID: {} and Total Amount: {}", savedSale.getId(), totalAmount);
        return savedSale;
    }

    public List<OfflineSale> getByCustomerId(String customerId) {
        log.info("Fetching offline sales for customer ID: {}", customerId);
        return offlineSaleRepository.findByCustomerId(customerId);
    }

    @Transactional
    public OfflineSale collectPayment(String id, BigDecimal amountPaid) {
        log.info("Collecting payment of {} for offline sale: {}", amountPaid, id);
        OfflineSale sale = getById(id);
        BigDecimal newPaid = sale.getPaidAmount().add(amountPaid);
        sale.setPaidAmount(newPaid);
        BigDecimal newPending = sale.getTotalAmount().subtract(newPaid);
        if (newPending.compareTo(BigDecimal.ZERO) <= 0) {
            sale.setPendingAmount(BigDecimal.ZERO);
            sale.setPaymentStatus(PaymentStatus.PAID);
        } else {
            sale.setPendingAmount(newPending);
            sale.setPaymentStatus(PaymentStatus.PARTIAL);
        }
        return offlineSaleRepository.save(sale);
    }

    @Transactional
    public OfflineSale markReturned(String id) {
        log.info("Marking offline sale: {} as returned", id);
        OfflineSale sale = getById(id);
        sale.setRentalStatus("RETURNED");

        // Restock items back to inventory
        for (OfflineSaleItem saleItem : sale.getItems()) {
            Item item = saleItem.getItem();
            item.setStock(item.getStock() + saleItem.getQuantity());
            itemRepository.save(item);
        }

        return offlineSaleRepository.save(sale);
    }
}
