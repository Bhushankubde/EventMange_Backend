package com.event.EventManage.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private String discountType; // PERCENTAGE or FIXED

    private LocalDate expiryDate;

    private boolean active = true;

    private int usageLimit = 100;

    private int usageCount = 0;
}
