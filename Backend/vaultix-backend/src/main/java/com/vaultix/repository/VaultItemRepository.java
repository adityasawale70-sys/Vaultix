package com.vaultix.repository;

import com.vaultix.entity.User;
import com.vaultix.entity.VaultCategory;
import com.vaultix.entity.VaultFolder;
import com.vaultix.entity.VaultItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultItemRepository extends JpaRepository<VaultItem, Long> {

    List<VaultItem> findByUserAndIsTrashedFalse(User user);

    List<VaultItem> findByUserAndCategoryAndIsTrashedFalse(User user, VaultCategory category);

    List<VaultItem> findByUserAndIsFavoriteTrueAndIsTrashedFalse(User user);

    List<VaultItem> findByUserAndIsTrashedTrue(User user);

    Optional<VaultItem> findByVaultItemIdAndUser(Long vaultItemId, User user);

    List<VaultItem> findByUserAndTitleContainingIgnoreCaseAndIsTrashedFalse(User user, String query);

    List<VaultItem> findByUserAndFolderAndIsTrashedFalse(User user, VaultFolder folder);

    List<VaultItem> findByUserAndFolderAndCategoryAndIsTrashedFalse(User user, VaultFolder folder, VaultCategory category);

    List<VaultItem> findByUserAndFolderAndTitleContainingIgnoreCaseAndIsTrashedFalse(User user, VaultFolder folder, String query);

    List<VaultItem> findByUserAndFolderAndIsFavoriteTrueAndIsTrashedFalse(User user, VaultFolder folder);

    List<VaultItem> findByFolder(VaultFolder folder);

    long countByUserAndIsTrashedFalse(User user);
}
