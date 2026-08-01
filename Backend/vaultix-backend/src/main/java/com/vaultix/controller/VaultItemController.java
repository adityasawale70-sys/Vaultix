package com.vaultix.controller;

import com.vaultix.dto.VaultItemRequest;
import com.vaultix.dto.VaultItemResponse;
import com.vaultix.entity.VaultCategory;
import com.vaultix.service.VaultItemService;
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
public class VaultItemController {

    private final VaultItemService vaultItemService;

    public VaultItemController(VaultItemService vaultItemService) {
        this.vaultItemService = vaultItemService;
    }

    @PostMapping
    public ResponseEntity<VaultItemResponse> createVaultItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VaultItemRequest request) {
        VaultItemResponse response = vaultItemService.createVaultItem(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VaultItemResponse>> getAllVaultItems(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) VaultCategory category,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) String query) {
        List<VaultItemResponse> response = vaultItemService.getAllVaultItems(
                userDetails.getUsername(), category, favorite, query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VaultItemResponse> getVaultItemById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        VaultItemResponse response = vaultItemService.getVaultItemById(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VaultItemResponse> updateVaultItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody VaultItemRequest request) {
        VaultItemResponse response = vaultItemService.updateVaultItem(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<VaultItemResponse> toggleFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        VaultItemResponse response = vaultItemService.toggleFavorite(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/trash")
    public ResponseEntity<VaultItemResponse> moveToTrash(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        VaultItemResponse response = vaultItemService.moveToTrash(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<VaultItemResponse> restoreFromTrash(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        VaultItemResponse response = vaultItemService.restoreFromTrash(userDetails.getUsername(), id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePermanently(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        vaultItemService.deleteVaultItemPermanently(userDetails.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "Vault item deleted permanently"));
    }

    @GetMapping("/trash")
    public ResponseEntity<List<VaultItemResponse>> getTrashedItems(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<VaultItemResponse> response = vaultItemService.getTrashedItems(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
