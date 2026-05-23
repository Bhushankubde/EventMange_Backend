package com.event.EventManage.service;
import com.event.EventManage.model.WalkInCustomer;
import com.event.EventManage.repository.WalkInCustomerRepository;
import com.event.EventManage.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalkInCustomerService {
    private final WalkInCustomerRepository repository;
    
    public List<WalkInCustomer> getAll() { 
        log.info("Fetching all walk-in customers");
        return repository.findAll(); 
    }
    
    public WalkInCustomer getById(String id) {
        log.info("Fetching walk-in customer with ID: {}", id);
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }
    
    public WalkInCustomer create(WalkInCustomer customer) { 
        log.info("Creating new walk-in customer: {} {}", customer.getFirstName(), customer.getLastName());
        return repository.save(customer); 
    }
    
    public WalkInCustomer searchByPhone(String phone) {
        log.info("Searching walk-in customer by phone: {}", phone);
        return repository.findByPhone(phone).orElseThrow(() -> new ResourceNotFoundException("Customer not found with phone: " + phone));
    }
}
