package com.vaultix.dto;

import com.vaultix.entity.AuditLog;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long logId;
    private String eventType;
    private String description;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;

    public static AuditLogResponse fromEntity(AuditLog log) {
        AuditLogResponse dto = new AuditLogResponse();
        dto.setLogId(log.getLogId());
        dto.setEventType(log.getEventType());
        dto.setDescription(log.getDescription());
        dto.setIpAddress(log.getIpAddress());
        dto.setUserAgent(log.getUserAgent());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
