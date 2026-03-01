package com.material.agent.repository;

import com.material.agent.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
    
    Long countByUserIdAndStatus(String userId, String status);
}
