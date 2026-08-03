package com.vaultix.service;

import com.vaultix.dto.VaultItemRequest;
import com.vaultix.dto.VaultItemResponse;
import com.vaultix.entity.VaultCategory;

import java.util.List;

public interface VaultItemService {

    VaultItemResponse createVaultItem(String userEmail, VaultItemRequest request);

    VaultItemResponse getVaultItemById(String userEmail, Long id);

    List<VaultItemResponse> getAllVaultItems(String userEmail, VaultCategory category, Boolean isFavorite, String query, Long folderId);

    VaultItemResponse updateVaultItem(String userEmail, Long id, VaultItemRequest request);

    VaultItemResponse toggleFavorite(String userEmail, Long id);

    VaultItemResponse moveToTrash(String userEmail, Long id);

    VaultItemResponse restoreFromTrash(String userEmail, Long id);

    void deleteVaultItemPermanently(String userEmail, Long id);

    List<VaultItemResponse> getTrashedItems(String userEmail);
}
