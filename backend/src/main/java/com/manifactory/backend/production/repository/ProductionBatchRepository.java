package com.manifactory.backend.production.repository;

import com.manifactory.backend.production.entity.ProductionBatch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, Long> {
    List<ProductionBatch> findByTenantId(Long tenantId);
    Optional<ProductionBatch> findByIdAndTenantId(Long id, Long tenantId);
}
