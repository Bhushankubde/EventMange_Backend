package com.event.EventManage.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vendors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String serviceType; // DJ, Decorator, Catering, etc.

    private String email;

    private String phone;

    private double rating = 5.0;

    private boolean active = true;
}
