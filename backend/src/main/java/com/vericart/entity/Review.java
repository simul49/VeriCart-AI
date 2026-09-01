package com.vericart.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long userId;
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String content;
    private String images;
    private String sentiment;
    private String emotion;
    private BigDecimal fakeProbability;
    private String fakeReason;
    private Integer isFlagged;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // joined fields
    private String username;
    private String userAvatar;

    // computed: 1 when the review is tied to a real order (verified purchase)
    private Integer verified;

    // joined from product (used by seller dashboard)
    private String productName;
}
