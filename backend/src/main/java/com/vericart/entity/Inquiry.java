package com.vericart.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** A customer question about a product, addressed to the store owner (seller). */
@Data
public class Inquiry {
    private Long id;
    private Long productId;
    private Long userId;
    private Long sellerId;
    private String message;
    private String reply;
    /** Who wrote the reply: AI (auto-reply) or SELLER (manual). */
    private String replySource;
    /** OPEN / REPLIED / CLOSED */
    private String status;
    /** 1 = seller has seen it */
    private Integer isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
