package com.vaultix.service;

import com.vaultix.dto.VaultItemVersionResponse;

import java.util.List;

public interface VaultItemVersionService {

    List<VaultItemVersionResponse> getItemVersionHistory(String userEmail, Long vaultItemId);

    void rollbackToVersion(String userEmail, Long vaultItemId, Long versionId);
}
