package com.manifactory.backend.production.service.impl;

import com.manifactory.backend.products.entity.Product;
import com.manifactory.backend.products.repository.ProductRepository;
import com.manifactory.backend.production.dto.CreateProductionBatchDTO;
import com.manifactory.backend.production.dto.ProductionBatchResponseDTO;
import com.manifactory.backend.production.entity.ProductionBatch;
import com.manifactory.backend.production.mapper.ProductionBatchMapper;
import com.manifactory.backend.production.repository.ProductionBatchRepository;
import com.manifactory.backend.production.service.ProductionBatchService;
import com.manifactory.backend.stock.dto.CreateStockEntryDTO;
import com.manifactory.backend.stock.entity.StockEntryType;
import com.manifactory.backend.stock.repository.StockEntryRepository;
import com.manifactory.backend.stock.service.StockService;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductionBatchServiceImpl implements ProductionBatchService {

    private final ProductionBatchRepository repository;
    private final ProductionBatchMapper mapper;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final StockEntryRepository stockEntryRepository;

    @Override
    public ProductionBatchResponseDTO create(Long tenantId, CreateProductionBatchDTO dto) {
        Product product = productRepository.findByIdAndTenantId(dto.getProductId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        ProductionBatch entity = mapper.toEntity(dto, tenantId);
        ProductionBatch saved = repository.save(entity);

        CreateStockEntryDTO stockDto = new CreateStockEntryDTO();
        stockDto.setType(StockEntryType.ENTRY);
        stockDto.setProductId(dto.getProductId());
        stockDto.setQuantity(dto.getQuantityKg());
        stockDto.setNote("Producción lote #" + saved.getId());
        stockDto.setEntryDate(dto.getBatchDate());
        stockService.create(tenantId, stockDto);

        return mapper.toResponse(saved, product.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionBatchResponseDTO> listByTenant(Long tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .sorted(Comparator
                        .comparing(ProductionBatch::getBatchDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ProductionBatch::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(batch -> {
                    String productName = productRepository.findById(batch.getProductId())
                            .map(Product::getName)
                            .orElse("Producto eliminado");
                    return mapper.toResponse(batch, productName);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long tenantId, Long id) {
        ProductionBatch batch = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Production batch not found"));
        String expectedNote = "Producción lote #" + batch.getId();
        stockEntryRepository.findByTenantId(tenantId).stream()
                .filter(entry -> expectedNote.equals(entry.getNote()))
                .findFirst()
                .ifPresent(stockEntryRepository::delete);
        repository.delete(batch);
    }
}
