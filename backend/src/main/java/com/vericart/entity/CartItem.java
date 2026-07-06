package com.vericart.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CartItem {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // joined fields
    private String productName;
    private String productImage;
    private java.math.BigDecimal productPrice;
}
