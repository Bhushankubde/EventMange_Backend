package com.event.EventManage.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ItemDTO {
    private String id;
    private String name;
    private String description;
    private String categoryId;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
}
