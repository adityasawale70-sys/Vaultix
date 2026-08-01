package com.vaultix.service.impl;

import com.vaultix.dto.VaultItemShareRequest;
import com.vaultix.dto.VaultItemShareResponse;
import com.vaultix.entity.User;
import com.vaultix.entity.VaultItem;
import com.vaultix.entity.VaultItemShare;
import com.vaultix.exception.ResourceNotFoundException;
import com.vaultix.repository.UserRepository;
import com.vaultix.repository.VaultItemRepository;
import com.vaultix.repository.VaultItemShareRepository;
import com.vaultix.service.AuditLogService;
import com.vaultix.service.VaultItemShareService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VaultItemShareServiceImpl implements VaultItemShareService {

    private final VaultItemRepository vaultItemRepository;
    private final VaultItemShareRepository shareRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public VaultItemShareServiceImpl(
            VaultItemRepository vaultItemRepository,
            VaultItemShareRepository shareRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.vaultItemRepository = vaultItemRepository;
        this.shareRepository = shareRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Override
    @Transactional
    public VaultItemShareResponse shareVaultItem(String ownerEmail, Long vaultItemId, VaultItemShareRequest request) {
        User owner = getUser(ownerEmail);
        VaultItem item = vaultItemRepository.findByVaultItemIdAndUser(vaultItemId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Vault item not found: " + vaultItemId));

        User targetUser = getUser(request.getSharedWithEmail());
        if (targetUser.getUserId().equals(owner.getUserId())) {
            throw new IllegalArgumentException("Cannot share a secret with yourself");
        }

        VaultItemShare share = shareRepository.findByVaultItemAndSharedWithUser(item, targetUser)
                .orElse(new VaultItemShare(item, owner, targetUser, request.getPermissionLevel()));

        share.setPermissionLevel(request.getPermissionLevel());
        share.setViewCountLimit(request.getViewCountLimit());
        share.setExpiresAt(request.getExpiresAt());

        VaultItemShare saved = shareRepository.save(share);
        auditLogService.logEvent(owner, "VAULT_ITEM_SHARE",
                "Shared vault item '" + item.getTitle() + "' with " + targetUser.getEmail(), null, null);

        return VaultItemShareResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaultItemShareResponse> getSharesForItem(String ownerEmail, Long vaultItemId) {
        User owner = getUser(ownerEmail);
        VaultItem item = vaultItemRepository.findByVaultItemIdAndUser(vaultItemId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Vault item not found: " + vaultItemId));

        return shareRepository.findByVaultItem(item)
                .stream()
                .map(VaultItemShareResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaultItemShareResponse> getItemsSharedWithUser(String userEmail) {
        User user = getUser(userEmail);
        return shareRepository.findBySharedWithUser(user)
                .stream()
                .map(VaultItemShareResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void revokeShare(String ownerEmail, Long shareId) {
        User owner = getUser(ownerEmail);
        VaultItemShare share = shareRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found: " + shareId));

        if (!share.getSharedByUser().getUserId().equals(owner.getUserId())) {
            throw new IllegalArgumentException("Only the secret owner can revoke shares");
        }

        shareRepository.delete(share);
        auditLogService.logEvent(owner, "VAULT_SHARE_REVOKE",
                "Revoked access to vault item for " + share.getSharedWithUser().getEmail(), null, null);
    }
}
