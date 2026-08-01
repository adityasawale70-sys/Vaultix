package com.vaultix.service;

import com.vaultix.dto.VaultItemShareRequest;
import com.vaultix.dto.VaultItemShareResponse;

import java.util.List;

public interface VaultItemShareService {

    VaultItemShareResponse shareVaultItem(String ownerEmail, Long vaultItemId, VaultItemShareRequest request);

    List<VaultItemShareResponse> getSharesForItem(String ownerEmail, Long vaultItemId);

    List<VaultItemShareResponse> getItemsSharedWithUser(String userEmail);

    void revokeShare(String ownerEmail, Long shareId);
}
