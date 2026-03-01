package com.material.agent.repository;

import com.material.agent.model.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    
    Optional<Material> findByMaterialCode(String materialCode);
    
    @Query("SELECT m FROM Material m WHERE m.name LIKE %:keyword% OR m.materialCode LIKE %:keyword%")
    List<Material> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT m FROM Material m WHERE m.name LIKE %:keyword% OR m.materialCode LIKE %:keyword%")
    Page<Material> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    List<Material> findByCurrentStockLessThanEqual(Integer safetyStock);
    
    List<Material> findByCategory(String category);
    
    Page<Material> findByCategory(String category, Pageable pageable);
    
    List<Material> findByStatus(String status);
    
    Page<Material> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT m FROM Material m WHERE m.currentStock <= m.safetyStock")
    List<Material> findLowStock();
    
    @Query("SELECT DISTINCT m.category FROM Material m WHERE m.category IS NOT NULL")
    List<String> findAllCategories();
}
