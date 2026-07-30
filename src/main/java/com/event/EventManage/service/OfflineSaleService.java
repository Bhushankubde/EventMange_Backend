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
    private final NotificationService notificationService;

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

            OfflineSaleItem saleItem = OfflineSaleItem.builder()
                    .sale(sale)
                    .item(item)
                    .quantity(requested)
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
        try {
            notificationService.sendNotification(
                "New Offline Rental logged: Invoice #" + savedSale.getId().substring(0, 6).toUpperCase() + 
                " for " + customer.getFirstName() + " " + customer.getLastName() + " (Total: ₹" + totalAmount + ")",
                "INFO"
            );
        } catch (Exception e) {
            log.error("Failed to send offline sale notification", e);
        }
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
        
        if (!"RETURNED".equalsIgnoreCase(sale.getRentalStatus())) {
            sale.setRentalStatus("RETURNED");

            // Restock items back to inventory
            for (OfflineSaleItem saleItem : sale.getItems()) {
                Item item = itemRepository.findByIdForUpdate(saleItem.getItem().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
                int currentAvailable = item.getAvailableQuantity() != null ? item.getAvailableQuantity() : 0;
                item.setAvailableQuantity(currentAvailable + saleItem.getQuantity());
                item.setStock(currentAvailable + saleItem.getQuantity());
                itemRepository.save(item);
                
                try {
                    notificationService.broadcastInventoryUpdate(item.getId(), item.getAvailableQuantity());
                } catch (Exception e) {
                    log.error("Failed to broadcast inventory update", e);
                }
            }
        }

        return offlineSaleRepository.save(sale);
    }
}
