package com.vaultix.repository;

import com.vaultix.entity.VaultItem;
import com.vaultix.entity.VaultItemVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VaultItemVersionRepository extends JpaRepository<VaultItemVersion, Long> {

    List<VaultItemVersion> findByVaultItemOrderByVersionNumberDesc(VaultItem vaultItem);

    long countByVaultItem(VaultItem vaultItem);
}
