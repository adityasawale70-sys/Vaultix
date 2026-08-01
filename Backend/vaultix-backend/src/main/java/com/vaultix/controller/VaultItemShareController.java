package com.vaultix.controller;

import com.vaultix.dto.VaultItemShareRequest;
import com.vaultix.dto.VaultItemShareResponse;
import com.vaultix.service.VaultItemShareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vault")
public class VaultItemShareController {

    private final VaultItemShareService shareService;

    public VaultItemShareController(VaultItemShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping("/{id}/shares")
    public ResponseEntity<VaultItemShareResponse> shareVaultItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody VaultItemShareRequest request) {
        VaultItemShareResponse response = shareService.shareVaultItem(userDetails.getUsername(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/shares")
    public ResponseEntity<List<VaultItemShareResponse>> getSharesForItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        List<VaultItemShareResponse> response = shareService.getSharesForItem(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<List<VaultItemShareResponse>> getItemsSharedWithUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<VaultItemShareResponse> response = shareService.getItemsSharedWithUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/shares/{shareId}")
    public ResponseEntity<Map<String, String>> revokeShare(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long shareId) {
        shareService.revokeShare(userDetails.getUsername(), shareId);
        return ResponseEntity.ok(Map.of("message", "Share revoked successfully"));
    }
}
