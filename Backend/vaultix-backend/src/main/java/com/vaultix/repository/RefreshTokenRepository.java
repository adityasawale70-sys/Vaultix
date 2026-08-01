package com.vaultix.repository;

import com.vaultix.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUsername(String username);

    /** Delete all tokens for a user (used on logout-all-devices). */
    @Modifying
    @Transactional
    void deleteAllByUsername(String username);

    /** Bulk-revoke expired tokens (run periodically for DB hygiene). */
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.expiry < :now AND rt.revoked = false")
    int revokeExpiredTokens(@Param("now") Instant now);
}