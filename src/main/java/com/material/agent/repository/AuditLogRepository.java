package com.material.agent.repository;

import com.material.agent.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(String userId);
}
