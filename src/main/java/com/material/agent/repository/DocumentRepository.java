package com.material.agent.repository;

import com.material.agent.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    Optional<Document> findByTitle(String title);
    
    List<Document> findByCategory(String category);
    
    List<Document> findByMaterialId(Long materialId);
    
    List<Document> findByStatus(String status);
    
    List<Document> findByVectorStatus(String vectorStatus);
    
    @Query("SELECT d FROM Document d WHERE d.status = 'active' AND d.vectorStatus = 'indexed'")
    List<Document> findActiveIndexedDocuments();
    
    @Query("SELECT d FROM Document d WHERE d.title LIKE %:keyword% OR d.content LIKE %:keyword%")
    List<Document> searchByKeyword(@Param("keyword") String keyword);
}
