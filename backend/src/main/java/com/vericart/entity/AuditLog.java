package com.vericart.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLog {
    private Long id;
    private Long userId;          // null for guest actions
    private String username;      // denormalized for easy reading
    private String action;        // e.g. LOGIN, ORDER_CREATED, REVIEW_CREATED
    private String category;      // LOGIN / ORDER / REVIEW / AI / ERROR
    private String targetType;    // optional: PRODUCT, ORDER, REVIEW...
    private Long targetId;        // optional
    private String detail;        // human-readable description
    private String ip;            // optional client IP
    private Integer isError;      // 1 = error entry (FR-070)
    private LocalDateTime createdAt;
}
