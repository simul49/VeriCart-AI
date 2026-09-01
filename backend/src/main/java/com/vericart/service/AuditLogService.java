package com.vericart.service;

import com.vericart.entity.AuditLog;
import com.vericart.mapper.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FR-069 Activity Logs + FR-070 Error Logs.
 * Records user activities (login, orders, review changes, AI processing)
 * and exceptions. Auditing must never break the main business flow, so all
 * record methods swallow exceptions and log a warning instead.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    /** Record a normal activity entry. */
    public void record(Long userId, String username, String action, String category,
                       String targetType, Long targetId, String detail) {
        insert(userId, username, action, category, targetType, targetId, detail, null, 0);
    }

    /** Record an error/exception entry (FR-070). */
    public void recordError(Long userId, String username, String action, String detail) {
        insert(userId, username, action, "ERROR", null, null, detail, null, 1);
    }

    public List<AuditLog> findRecent(int limit) {
        return auditLogMapper.findRecent(limit);
    }

    public List<AuditLog> findByCategory(String category, int limit) {
        return auditLogMapper.findByCategory(category, limit);
    }

    public List<AuditLog> findErrors(int limit) {
        return auditLogMapper.findErrors(limit);
    }

    public int countAll() {
        return auditLogMapper.countAll();
    }

    public int countErrors() {
        return auditLogMapper.countErrors();
    }

    private void insert(Long userId, String username, String action, String category,
                        String targetType, Long targetId, String detail, String ip, int isError) {
        AuditLog entry = new AuditLog();
        entry.setUserId(userId);
        entry.setUsername(username);
        entry.setAction(action);
        entry.setCategory(category);
        entry.setTargetType(targetType);
        entry.setTargetId(targetId);
        entry.setDetail(detail);
        entry.setIp(ip);
        entry.setIsError(isError);
        try {
            auditLogMapper.insert(entry);
        } catch (Exception e) {
            log.warn("[Audit] Failed to persist audit log entry: {}", e.getMessage());
        }
    }
}
