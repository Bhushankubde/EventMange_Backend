package com.event.EventManage.repository;

import com.event.EventManage.model.OfflineSale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfflineSaleRepository extends JpaRepository<OfflineSale, String> {
    List<OfflineSale> findByCustomerId(String customerId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OfflineSale o")
    java.math.BigDecimal sumTotalAmount();
}
