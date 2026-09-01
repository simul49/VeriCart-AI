package com.vericart.service;

import com.vericart.entity.Notification;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public List<Notification> getByUser(Long userId, int limit) {
        return notificationMapper.findByUser(userId, limit);
    }

    public int countUnread(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    /** Convenience helper used by other services to push notifications. */
    public void push(Long userId, String title, String content, String type) {
        try {
            notificationMapper.insert(userId, title, content, type);
        } catch (Exception e) {
            // Notifications must never break the main business flow
        }
    }

    public void markRead(Long id, Long userId) {
        Notification n = notificationMapper.findById(id);
        if (n == null) throw new BusinessException(404, "Notification not found");
        if (!n.getUserId().equals(userId)) throw new BusinessException(403, "Access denied");
        notificationMapper.markAsRead(id, userId);
    }

    public void markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
    }

    public void delete(Long id, Long userId) {
        notificationMapper.delete(id, userId);
    }

    /** Bundles list + unread count for the frontend bell component. */
    public Map<String, Object> summary(Long userId, int limit) {
        Map<String, Object> m = new HashMap<>();
        m.put("items", notificationMapper.findByUser(userId, limit));
        m.put("unread", notificationMapper.countUnread(userId));
        return m;
    }
}
