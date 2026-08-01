package com.vaultix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VaultFolderRequest {

    @NotBlank(message = "Folder name is required")
    @Size(max = 100, message = "Folder name cannot exceed 100 characters")
    private String name;

    private String colorCode = "#6366f1";

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
}
