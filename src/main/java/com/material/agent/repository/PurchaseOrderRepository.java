package com.material.agent.repository;

import com.material.agent.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    Optional<PurchaseOrder> findByOrderNo(String orderNo);
    
    List<PurchaseOrder> findByStatus(String status);
    
    List<PurchaseOrder> findByRequesterId(String requesterId);
    
    List<PurchaseOrder> findByApproverId(String approverId);
    
    @Query("SELECT p FROM PurchaseOrder p WHERE p.status = :status ORDER BY p.createdAt DESC")
    List<PurchaseOrder> findByStatusOrderByCreatedAtDesc(@Param("status") String status);
    
    @Query("SELECT COUNT(p) FROM PurchaseOrder p WHERE p.status = :status")
    Long countByStatus(@Param("status") String status);
}
