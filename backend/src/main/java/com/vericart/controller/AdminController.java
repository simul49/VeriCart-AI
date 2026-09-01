package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.entity.*;
import com.vericart.mapper.UserMapper;
import com.vericart.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    /** FR-069 — activity audit log for admins. */
    @GetMapping("/audit-logs")
    public Result<List<com.vericart.entity.AuditLog>> auditLogs(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "50") int limit) {
        if (category != null && !category.isBlank()) {
            return Result.success(auditLogService.findByCategory(category, Math.min(limit, 200)));
        }
        return Result.success(auditLogService.findRecent(Math.min(limit, 200)));
    }

    /** FR-070 — error logs for admins. */
    @GetMapping("/audit-logs/errors")
    public Result<List<com.vericart.entity.AuditLog>> errorLogs(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(auditLogService.findErrors(Math.min(limit, 200)));
    }

    /** Audit stats for the admin dashboard. */
    @GetMapping("/audit-logs/stats")
    public Result<Map<String, Object>> auditStats() {
        return Result.success(Map.of(
                "totalEntries", auditLogService.countAll(),
                "totalErrors", auditLogService.countErrors()
        ));
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(Map.of(
                "totalProducts", productService.count(),
                "totalOrders", orderService.count(),
                "totalRevenue", orderService.totalRevenue(),
                "flaggedReviews", reviewService.findFlaggedReviews().size()
        ));
    }

    @GetMapping("/orders")
    public Result<List<Order>> orders(@RequestParam(required = false) String status) {
        return Result.success(orderService.findAll(status));
    }

    @PutMapping("/orders/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.updateStatus(id, body.get("status"));
        return Result.success(null);
    }

    @GetMapping("/reviews/flagged")
    public Result<List<Review>> flaggedReviews() {
        return Result.success(reviewService.findFlaggedReviews());
    }

    @GetMapping("/products")
    public Result<List<Product>> products() {
        return Result.success(productService.findAll());
    }

    @GetMapping("/users")
    public Result<List<User>> users() {
        return Result.success(userMapper.findAll());
    }
}
