package com.manifactory.backend.finance.service;

import com.manifactory.backend.finance.dto.CreateFinanceEntryDTO;
import com.manifactory.backend.finance.dto.FinanceEntryResponseDTO;
import com.manifactory.backend.finance.dto.FinanceSummaryDTO;
import java.time.YearMonth;
import java.util.List;

public interface FinanceService {
    FinanceEntryResponseDTO create(Long tenantId, CreateFinanceEntryDTO dto);

    List<FinanceEntryResponseDTO> listByTenant(Long tenantId, YearMonth period);

    FinanceSummaryDTO summary(Long tenantId, YearMonth period);

    void delete(Long tenantId, Long id);
}
