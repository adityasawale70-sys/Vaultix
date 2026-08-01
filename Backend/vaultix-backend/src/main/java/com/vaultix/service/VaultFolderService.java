package com.vaultix.service;

import com.vaultix.dto.VaultFolderRequest;
import com.vaultix.dto.VaultFolderResponse;

import java.util.List;

public interface VaultFolderService {

    VaultFolderResponse createFolder(String userEmail, VaultFolderRequest request);

    List<VaultFolderResponse> getUserFolders(String userEmail);

    void deleteFolder(String userEmail, Long folderId);
}
