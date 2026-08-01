package com.vaultix.controller;

import com.vaultix.dto.VaultFolderRequest;
import com.vaultix.dto.VaultFolderResponse;
import com.vaultix.service.VaultFolderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
public class VaultFolderController {

    private final VaultFolderService folderService;

    public VaultFolderController(VaultFolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public ResponseEntity<VaultFolderResponse> createFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VaultFolderRequest request) {
        VaultFolderResponse response = folderService.createFolder(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VaultFolderResponse>> getUserFolders(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<VaultFolderResponse> response = folderService.getUserFolders(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        folderService.deleteFolder(userDetails.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "Folder deleted successfully"));
    }
}
