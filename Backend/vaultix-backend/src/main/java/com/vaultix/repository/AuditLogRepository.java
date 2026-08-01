package com.vaultix.repository;

import com.vaultix.entity.AuditLog;
import com.vaultix.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop50ByUserOrderByCreatedAtDesc(User user);

    List<AuditLog> findTop100ByOrderByCreatedAtDesc();
}
