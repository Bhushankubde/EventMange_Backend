package com.event.EventManage.repository;

import com.event.EventManage.model.WalkInCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalkInCustomerRepository extends JpaRepository<WalkInCustomer, String> {
    Optional<WalkInCustomer> findByPhone(String phone);
}
