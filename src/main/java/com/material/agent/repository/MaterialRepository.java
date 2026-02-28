package com.material.agent.repository;

import com.material.agent.model.Material;
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
    List<Material> findByKeyword(@Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);
    
    List<Material> findByCurrentStockLessThanEqual(Integer safetyStock);
    
    List<Material> findByCategory(String category);
    
    List<Material> findByStatus(String status);
}
