package com.vericart.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Long categoryId;
    private String brand;
    private String images;
    private String specifications;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer trustScore;
    private String trustLevel;
    private String aiSummary;
    private LocalDateTime aiSummaryTime;
    private Integer fakeReviewCount;
    private Integer status;
    private Long sellerId;
    private String externalUrl;
    private String variants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
