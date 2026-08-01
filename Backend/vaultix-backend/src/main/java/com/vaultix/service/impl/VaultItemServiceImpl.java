package com.vaultix.service.impl;

import com.vaultix.dto.VaultItemRequest;
import com.vaultix.dto.VaultItemResponse;
import com.vaultix.entity.User;
import com.vaultix.entity.VaultCategory;
import com.vaultix.entity.VaultItem;
import com.vaultix.exception.ResourceNotFoundException;
import com.vaultix.repository.UserRepository;
import com.vaultix.repository.VaultItemRepository;
import com.vaultix.service.AuditLogService;
import com.vaultix.service.VaultItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VaultItemServiceImpl implements VaultItemService {

    private final VaultItemRepository vaultItemRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public VaultItemServiceImpl(
            VaultItemRepository vaultItemRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.vaultItemRepository = vaultItemRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException("Vault item not found with id: " + id));
    }

    @Override
    @Transactional
    public VaultItemResponse createVaultItem(String userEmail, VaultItemRequest request) {
        User user = getUser(userEmail);

        VaultItem item = new VaultItem();
        item.setUser(user);
        item.setCategory(request.getCategory());
        item.setTitle(request.getTitle());
        item.setUsernameOrIdentifier(request.getUsernameOrIdentifier());
        item.setUrl(request.getUrl());
        item.setEncryptedPayload(request.getEncryptedPayload());
        item.setIv(request.getIv());
        item.setAuthTag(request.getAuthTag());
        item.setIsFavorite(request.getIsFavorite() != null ? request.getIsFavorite() : false);
        item.setIsTrashed(false);

        if (request.getTags() != null) {
            item.setTags(request.getTags());
        }

        VaultItem saved = vaultItemRepository.save(item);
        auditLogService.logEvent(user, "VAULT_ITEM_CREATE", "Created vault item: " + saved.getTitle(), null, null);

        return VaultItemResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VaultItemResponse getVaultItemById(String userEmail, Long id) {
        VaultItem item = getItemForUser(userEmail, id);
        auditLogService.logEvent(item.getUser(), "VAULT_ITEM_ACCESS", "Accessed vault item: " + item.getTitle(), null, null);
        return VaultItemResponse.fromEntity(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaultItemResponse> getAllVaultItems(String userEmail, VaultCategory category, Boolean isFavorite, String query) {
        User user = getUser(userEmail);
        List<VaultItem> items;

        if (query != null && !query.trim().isEmpty()) {
            items = vaultItemRepository.findByUserAndTitleContainingIgnoreCaseAndIsTrashedFalse(user, query.trim());
        } else if (Boolean.TRUE.equals(isFavorite)) {
            items = vaultItemRepository.findByUserAndIsFavoriteTrueAndIsTrashedFalse(user);
        } else if (category != null) {
            items = vaultItemRepository.findByUserAndCategoryAndIsTrashedFalse(user, category);
        } else {
            items = vaultItemRepository.findByUserAndIsTrashedFalse(user);
        }

        return items.stream().map(VaultItemResponse::fromEntity).toList();
    }

    @Override
    @Transactional
    public VaultItemResponse updateVaultItem(String userEmail, Long id, VaultItemRequest request) {
        VaultItem item = getItemForUser(userEmail, id);

        item.setCategory(request.getCategory());
        item.setTitle(request.getTitle());
        item.setUsernameOrIdentifier(request.getUsernameOrIdentifier());
        item.setUrl(request.getUrl());
        item.setEncryptedPayload(request.getEncryptedPayload());
        item.setIv(request.getIv());
        item.setAuthTag(request.getAuthTag());
        if (request.getIsFavorite() != null) {
            item.setIsFavorite(request.getIsFavorite());
        }
        if (request.getTags() != null) {
            item.setTags(request.getTags());
        }

        VaultItem updated = vaultItemRepository.save(item);
        auditLogService.logEvent(item.getUser(), "VAULT_ITEM_UPDATE", "Updated vault item: " + updated.getTitle(), null, null);

        return VaultItemResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public VaultItemResponse toggleFavorite(String userEmail, Long id) {
        VaultItem item = getItemForUser(userEmail, id);
        item.setIsFavorite(!item.getIsFavorite());
        VaultItem updated = vaultItemRepository.save(item);
        return VaultItemResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public VaultItemResponse moveToTrash(String userEmail, Long id) {
        VaultItem item = getItemForUser(userEmail, id);
        item.setIsTrashed(true);
        item.setDeletedAt(LocalDateTime.now());
        VaultItem updated = vaultItemRepository.save(item);
        auditLogService.logEvent(item.getUser(), "VAULT_ITEM_TRASH", "Moved item to trash: " + updated.getTitle(), null, null);
        return VaultItemResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public VaultItemResponse restoreFromTrash(String userEmail, Long id) {
        VaultItem item = getItemForUser(userEmail, id);
        item.setIsTrashed(false);
        item.setDeletedAt(null);
        VaultItem updated = vaultItemRepository.save(item);
        auditLogService.logEvent(item.getUser(), "VAULT_ITEM_RESTORE", "Restored item from trash: " + updated.getTitle(), null, null);
        return VaultItemResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteVaultItemPermanently(String userEmail, Long id) {
        VaultItem item = getItemForUser(userEmail, id);
        vaultItemRepository.delete(item);
        auditLogService.logEvent(item.getUser(), "VAULT_ITEM_DELETE", "Permanently deleted item: " + item.getTitle(), null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaultItemResponse> getTrashedItems(String userEmail) {
        User user = getUser(userEmail);
        return vaultItemRepository.findByUserAndIsTrashedTrue(user)
                .stream()
                .map(VaultItemResponse::fromEntity)
                .toList();
    }
}
