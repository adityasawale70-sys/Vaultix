package com.vaultix.controller;

import com.vaultix.dto.AuditLogResponse;
import com.vaultix.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getUserAuditLogs(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AuditLogResponse> response = auditLogService.getUserAuditLogs(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
