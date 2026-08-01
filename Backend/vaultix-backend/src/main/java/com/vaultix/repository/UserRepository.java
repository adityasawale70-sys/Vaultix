package com.vaultix.repository;

import com.vaultix.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Find users missing a stored per-user salt (null only)
    java.util.List<com.vaultix.entity.User> findAllBySaltIsNull();
}