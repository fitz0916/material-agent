package com.material.agent.repository;

import com.material.agent.model.ToolExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ToolExecutionRepository extends JpaRepository<ToolExecution, Long> {
    
    List<ToolExecution> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    List<ToolExecution> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<ToolExecution> findByToolName(String toolName);
    
    @Query("SELECT t FROM ToolExecution t WHERE t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<ToolExecution> findRecentExecutions(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(t) FROM ToolExecution t WHERE t.toolName = :toolName AND t.status = :status")
    Long countByToolNameAndStatus(@Param("toolName") String toolName, @Param("status") String status);
}
