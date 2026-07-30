package com.event.EventManage.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Category category;

    @Transient
    private String tempCategoryId;

    @com.fasterxml.jackson.annotation.JsonProperty("categoryId")
    public String getCategoryId() {
        return category != null ? category.getId() : tempCategoryId;
    }


    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "total_quantity", columnDefinition = "INT DEFAULT 0")
    private Integer totalQuantity = 0;

    @Column(name = "available_quantity", columnDefinition = "INT DEFAULT 0")
    private Integer availableQuantity = 0;

    @Column(name = "stock", columnDefinition = "INT DEFAULT 0")
    private Integer stock = 0;

    private String imageUrl;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean available = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Integer getStock() {
        return this.availableQuantity != null ? this.availableQuantity : 0;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
        this.availableQuantity = stock;
        if (this.totalQuantity == null || this.totalQuantity == 0) {
            this.totalQuantity = stock;
        }
    }

}
