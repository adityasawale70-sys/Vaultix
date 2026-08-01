package com.vaultix.service.impl;

import com.vaultix.dto.AuditLogResponse;
import com.vaultix.entity.AuditLog;
import com.vaultix.entity.User;
import com.vaultix.repository.AuditLogRepository;
import com.vaultix.repository.UserRepository;
import com.vaultix.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void logEvent(User user, String eventType, String description, String ipAddress, String userAgent) {
        AuditLog auditLog = new AuditLog(user, eventType, description, ipAddress, userAgent);
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getUserAuditLogs(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        return auditLogRepository.findTop50ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
