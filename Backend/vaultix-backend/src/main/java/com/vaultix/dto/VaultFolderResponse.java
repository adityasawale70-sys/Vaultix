package com.vaultix.dto;

import com.vaultix.entity.VaultFolder;

import java.time.LocalDateTime;

public class VaultFolderResponse {

    private Long folderId;
    private String name;
    private String colorCode;
    private LocalDateTime createdAt;

    public static VaultFolderResponse fromEntity(VaultFolder folder) {
        VaultFolderResponse res = new VaultFolderResponse();
        res.setFolderId(folder.getFolderId());
        res.setName(folder.getName());
        res.setColorCode(folder.getColorCode());
        res.setCreatedAt(folder.getCreatedAt());
        return res;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
