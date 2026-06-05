package com.manifactory.backend.finance.repository;

import com.manifactory.backend.finance.entity.FinanceEntry;
import com.manifactory.backend.finance.entity.FinanceEntryType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanceEntryRepository extends JpaRepository<FinanceEntry, Long> {
    List<FinanceEntry> findByTenantIdOrderByEntryDateDescCreatedAtDesc(Long tenantId);

    Optional<FinanceEntry> findByIdAndTenantId(Long id, Long tenantId);

    List<FinanceEntry> findByTenantIdAndEntryDateBetween(Long tenantId, LocalDate start, LocalDate end);

    List<FinanceEntry> findByTenantIdAndTypeAndEntryDateBetween(Long tenantId, FinanceEntryType type, LocalDate start, LocalDate end);
}
