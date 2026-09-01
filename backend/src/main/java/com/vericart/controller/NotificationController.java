package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.entity.Notification;
import com.vericart.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "30") int limit,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(notificationService.summary(userId, limit));
    }

    @GetMapping("/unread-count")
    public Result<Integer> unreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(notificationService.countUnread(userId));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        notificationService.markRead(id, userId);
        return Result.success("Marked as read", null);
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        notificationService.markAllRead(userId);
        return Result.success("All marked as read", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        notificationService.delete(id, userId);
        return Result.success("Deleted", null);
    }
}
