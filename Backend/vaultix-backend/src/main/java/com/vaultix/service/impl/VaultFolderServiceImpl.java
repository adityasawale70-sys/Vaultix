package com.vaultix.service.impl;

import com.vaultix.dto.VaultFolderRequest;
import com.vaultix.dto.VaultFolderResponse;
import com.vaultix.entity.User;
import com.vaultix.entity.VaultFolder;
import com.vaultix.exception.ResourceNotFoundException;
import com.vaultix.repository.UserRepository;
import com.vaultix.repository.VaultFolderRepository;
import com.vaultix.service.AuditLogService;
import com.vaultix.service.VaultFolderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VaultFolderServiceImpl implements VaultFolderService {

    private final VaultFolderRepository folderRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public VaultFolderServiceImpl(
            VaultFolderRepository folderRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Override
    @Transactional
    public VaultFolderResponse createFolder(String userEmail, VaultFolderRequest request) {
        User user = getUser(userEmail);

        VaultFolder folder = new VaultFolder(user, request.getName(), request.getColorCode());
        VaultFolder saved = folderRepository.save(folder);

        auditLogService.logEvent(user, "FOLDER_CREATE", "Created vault folder: " + saved.getName(), null, null);
        return VaultFolderResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaultFolderResponse> getUserFolders(String userEmail) {
        User user = getUser(userEmail);
        return folderRepository.findByUserOrderByNameAsc(user)
                .stream()
                .map(VaultFolderResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteFolder(String userEmail, Long folderId) {
        User user = getUser(userEmail);
        VaultFolder folder = folderRepository.findByFolderIdAndUser(folderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found: " + folderId));

        folderRepository.delete(folder);
        auditLogService.logEvent(user, "FOLDER_DELETE", "Deleted folder: " + folder.getName(), null, null);
    }
}
