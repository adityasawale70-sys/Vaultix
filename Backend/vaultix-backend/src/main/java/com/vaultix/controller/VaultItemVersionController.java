package com.vaultix.controller;

import com.vaultix.dto.VaultItemVersionResponse;
import com.vaultix.service.VaultItemVersionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vault")
public class VaultItemVersionController {

    private final VaultItemVersionService versionService;

    public VaultItemVersionController(VaultItemVersionService versionService) {
        this.versionService = versionService;
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<VaultItemVersionResponse>> getItemVersions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        List<VaultItemVersionResponse> response = versionService.getItemVersionHistory(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/versions/{versionId}/rollback")
    public ResponseEntity<Map<String, String>> rollbackToVersion(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @PathVariable Long versionId) {
        versionService.rollbackToVersion(userDetails.getUsername(), id, versionId);
        return ResponseEntity.ok(Map.of("message", "Rolled back to version successfully"));
    }
}
