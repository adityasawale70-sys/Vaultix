package com.vaultix.service;

import com.vaultix.dto.AuditLogResponse;
import com.vaultix.entity.User;

import java.util.List;

public interface AuditLogService {

    void logEvent(User user, String eventType, String description, String ipAddress, String userAgent);

    List<AuditLogResponse> getUserAuditLogs(String email);
}
