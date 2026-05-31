package com.wesjou.keymanager.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
}
