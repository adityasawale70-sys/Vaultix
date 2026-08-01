package com.vaultix.repository;

import com.vaultix.entity.User;
import com.vaultix.entity.VaultItem;
import com.vaultix.entity.VaultItemShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultItemShareRepository extends JpaRepository<VaultItemShare, Long> {

    List<VaultItemShare> findBySharedWithUser(User user);

    List<VaultItemShare> findByVaultItem(VaultItem vaultItem);

    Optional<VaultItemShare> findByVaultItemAndSharedWithUser(VaultItem vaultItem, User sharedWithUser);
}
