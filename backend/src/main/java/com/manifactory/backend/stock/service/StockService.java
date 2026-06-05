package com.manifactory.backend.stock.service;

import com.manifactory.backend.stock.dto.CreateStockEntryDTO;
import com.manifactory.backend.stock.dto.StockEntryResponseDTO;
import com.manifactory.backend.stock.dto.StockLevelDTO;
import java.util.List;

public interface StockService {
    StockEntryResponseDTO create(Long tenantId, CreateStockEntryDTO dto);
    List<StockEntryResponseDTO> listByTenant(Long tenantId);
    List<StockLevelDTO> currentLevels(Long tenantId);
    void delete(Long tenantId, Long id);
}
