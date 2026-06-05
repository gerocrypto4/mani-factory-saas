package com.manifactory.backend.stock.service.impl;

import com.manifactory.backend.products.entity.Product;
import com.manifactory.backend.products.repository.ProductRepository;
import com.manifactory.backend.stock.dto.CreateStockEntryDTO;
import com.manifactory.backend.stock.dto.StockEntryResponseDTO;
import com.manifactory.backend.stock.dto.StockLevelDTO;
import com.manifactory.backend.stock.entity.StockEntry;
import com.manifactory.backend.stock.entity.StockEntryType;
import com.manifactory.backend.stock.mapper.StockEntryMapper;
import com.manifactory.backend.stock.repository.StockEntryRepository;
import com.manifactory.backend.stock.service.StockService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

    private final StockEntryRepository repository;
    private final StockEntryMapper mapper;
    private final ProductRepository productRepository;

    @Override
    public StockEntryResponseDTO create(Long tenantId, CreateStockEntryDTO dto) {
        Product product = productRepository.findByIdAndTenantId(dto.getProductId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        StockEntry entity = mapper.toEntity(dto, tenantId);
        StockEntry saved = repository.save(entity);
        return mapper.toResponse(saved, product.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockEntryResponseDTO> listByTenant(Long tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .sorted(Comparator
                        .comparing(StockEntry::getEntryDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(StockEntry::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(entry -> {
                    String productName = productRepository.findById(entry.getProductId())
                            .map(Product::getName)
                            .orElse("Producto eliminado");
                    return mapper.toResponse(entry, productName);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockLevelDTO> currentLevels(Long tenantId) {
        List<StockEntry> entries = repository.findByTenantId(tenantId);

        Map<Long, List<StockEntry>> byProduct = entries.stream()
                .collect(Collectors.groupingBy(StockEntry::getProductId));

        List<StockLevelDTO> levels = new ArrayList<>();
        for (Map.Entry<Long, List<StockEntry>> group : byProduct.entrySet()) {
            Long productId = group.getKey();
            List<StockEntry> productEntries = group.getValue();

            BigDecimal totalEntry = productEntries.stream()
                    .filter(e -> e.getType() == StockEntryType.ENTRY)
                    .map(e -> e.getQuantity() == null ? BigDecimal.ZERO : e.getQuantity())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalWithdrawal = productEntries.stream()
                    .filter(e -> e.getType() == StockEntryType.WITHDRAWAL)
                    .map(e -> e.getQuantity() == null ? BigDecimal.ZERO : e.getQuantity())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal currentQuantity = totalEntry.subtract(totalWithdrawal);

            LocalDate lastMovement = productEntries.stream()
                    .map(StockEntry::getEntryDate)
                    .filter(d -> d != null)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            String productName = productRepository.findById(productId)
                    .map(Product::getName)
                    .orElse("Producto eliminado");

            levels.add(new StockLevelDTO(productId, productName, currentQuantity, lastMovement));
        }

        levels.sort(Comparator.comparing(StockLevelDTO::getProductName,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return levels;
    }

    @Override
    public void delete(Long tenantId, Long id) {
        StockEntry entry = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Stock entry not found"));
        repository.delete(entry);
    }
}
