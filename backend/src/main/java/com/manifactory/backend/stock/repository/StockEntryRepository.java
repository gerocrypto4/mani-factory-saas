package com.manifactory.backend.stock.repository;

import com.manifactory.backend.stock.entity.StockEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockEntryRepository extends JpaRepository<StockEntry, Long> {
    List<StockEntry> findByTenantId(Long tenantId);
    Optional<StockEntry> findByIdAndTenantId(Long id, Long tenantId);
}
