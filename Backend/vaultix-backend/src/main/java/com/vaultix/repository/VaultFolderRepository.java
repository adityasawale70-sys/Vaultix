package com.vaultix.repository;

import com.vaultix.entity.User;
import com.vaultix.entity.VaultFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultFolderRepository extends JpaRepository<VaultFolder, Long> {

    List<VaultFolder> findByUserOrderByNameAsc(User user);

    Optional<VaultFolder> findByFolderIdAndUser(Long folderId, User user);
}
