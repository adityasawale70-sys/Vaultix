package com.vaultix.service.impl;

import com.vaultix.dto.VaultItemVersionResponse;
import com.vaultix.entity.User;
import com.vaultix.entity.VaultItem;
import com.vaultix.entity.VaultItemVersion;
import com.vaultix.exception.ResourceNotFoundException;
import com.vaultix.repository.UserRepository;
import com.vaultix.repository.VaultItemRepository;
import com.vaultix.repository.VaultItemVersionRepository;
import com.vaultix.service.AuditLogService;
import com.vaultix.service.VaultItemVersionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VaultItemVersionServiceImpl implements VaultItemVersionService {

    private final VaultItemRepository vaultItemRepository;
    private final VaultItemVersionRepository versionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public VaultItemVersionServiceImpl(
            VaultItemRepository vaultItemRepository,
            VaultItemVersionRepository versionRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.vaultItemRepository = vaultItemRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private VaultItem getItemForUser(String email, Long id) {
        User user = getUser(email);
        return vaultItemRepository.findByVaultItemIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Vault item not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaultItemVersionResponse> getItemVersionHistory(String userEmail, Long vaultItemId) {
        VaultItem item = getItemForUser(userEmail, vaultItemId);
        return versionRepository.findByVaultItemOrderByVersionNumberDesc(item)
                .stream()
                .map(VaultItemVersionResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void rollbackToVersion(String userEmail, Long vaultItemId, Long versionId) {
        VaultItem item = getItemForUser(userEmail, vaultItemId);
        VaultItemVersion targetVersion = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found: " + versionId));

        if (!targetVersion.getVaultItem().getVaultItemId().equals(vaultItemId)) {
            throw new IllegalArgumentException("Version does not belong to target vault item");
        }

        // Apply historical encrypted payload back to item
        item.setEncryptedPayload(targetVersion.getEncryptedPayload());
        item.setIv(targetVersion.getIv());
        item.setAuthTag(targetVersion.getAuthTag());
        vaultItemRepository.save(item);

        auditLogService.logEvent(item.getUser(), "VAULT_ITEM_ROLLBACK",
                "Rolled back vault item '" + item.getTitle() + "' to version #" + targetVersion.getVersionNumber(), null, null);
    }
}
