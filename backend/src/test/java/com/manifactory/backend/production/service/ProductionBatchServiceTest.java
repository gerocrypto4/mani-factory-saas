package com.manifactory.backend.production.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.manifactory.backend.products.entity.Product;
import com.manifactory.backend.products.repository.ProductRepository;
import com.manifactory.backend.production.dto.CreateProductionBatchDTO;
import com.manifactory.backend.production.dto.ProductionBatchResponseDTO;
import com.manifactory.backend.production.entity.ProductionBatch;
import com.manifactory.backend.production.mapper.ProductionBatchMapper;
import com.manifactory.backend.production.repository.ProductionBatchRepository;
import com.manifactory.backend.production.service.impl.ProductionBatchServiceImpl;
import com.manifactory.backend.stock.dto.CreateStockEntryDTO;
import com.manifactory.backend.stock.entity.StockEntry;
import com.manifactory.backend.stock.entity.StockEntryType;
import com.manifactory.backend.stock.repository.StockEntryRepository;
import com.manifactory.backend.stock.service.StockService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductionBatchServiceTest {

    @Mock
    private ProductionBatchRepository repository;

    @Mock
    private ProductionBatchMapper mapper;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    @Mock
    private StockEntryRepository stockEntryRepository;

    @InjectMocks
    private ProductionBatchServiceImpl service;

    @Test
    void createBatch_withValidProduct_savesAndCreatesStockEntry() {
        Long tenantId = 1L;
        CreateProductionBatchDTO dto = new CreateProductionBatchDTO();
        dto.setProductId(10L);
        dto.setQuantityKg(BigDecimal.valueOf(50));
        dto.setBatchDate(LocalDate.now());

        Product product = Product.builder().id(10L).tenantId(tenantId).name("Maní Salado").build();
        ProductionBatch entity = ProductionBatch.builder()
                .tenantId(tenantId).productId(10L)
                .quantityKg(BigDecimal.valueOf(50)).batchDate(LocalDate.now()).status("COMPLETED").build();
        ProductionBatch saved = ProductionBatch.builder()
                .id(5L).tenantId(tenantId).productId(10L)
                .quantityKg(BigDecimal.valueOf(50)).batchDate(LocalDate.now()).status("COMPLETED").build();

        when(productRepository.findByIdAndTenantId(10L, tenantId)).thenReturn(Optional.of(product));
        when(mapper.toEntity(dto, tenantId)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(any(ProductionBatch.class), eq("Maní Salado")))
                .thenReturn(new ProductionBatchResponseDTO());

        service.create(tenantId, dto);

        verify(repository).save(entity);
        verify(stockService).create(eq(tenantId), any(CreateStockEntryDTO.class));
    }

    @Test
    void createBatch_withProductFromOtherTenant_throws() {
        Long tenantId = 1L;
        CreateProductionBatchDTO dto = new CreateProductionBatchDTO();
        dto.setProductId(99L);
        dto.setQuantityKg(BigDecimal.valueOf(30));
        dto.setBatchDate(LocalDate.now());

        when(productRepository.findByIdAndTenantId(99L, tenantId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(tenantId, dto));
        assertTrue(ex.getMessage().contains("Product not found"));
    }

    @Test
    void deleteBatch_removesStockEntry() {
        Long tenantId = 1L;
        Long batchId = 5L;

        ProductionBatch batch = ProductionBatch.builder()
                .id(batchId).tenantId(tenantId).productId(10L)
                .quantityKg(BigDecimal.valueOf(100)).batchDate(LocalDate.now()).status("COMPLETED").build();

        StockEntry stockEntry = StockEntry.builder()
                .id(99L).tenantId(tenantId).productId(10L)
                .type(StockEntryType.ENTRY).quantity(BigDecimal.valueOf(100))
                .note("Producción lote #" + batchId).entryDate(LocalDate.now()).build();

        when(repository.findByIdAndTenantId(batchId, tenantId)).thenReturn(Optional.of(batch));
        when(stockEntryRepository.findByTenantId(tenantId)).thenReturn(List.of(stockEntry));

        service.delete(tenantId, batchId);

        verify(stockEntryRepository).delete(stockEntry);
        verify(repository).delete(batch);
    }

    @Test
    void deleteBatch_notBelongingToTenant_throws() {
        when(repository.findByIdAndTenantId(77L, 1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.delete(1L, 77L));
        assertTrue(ex.getMessage().contains("Production batch not found"));
    }

    @Test
    void listByTenant_returnsOrderedByDateDesc() {
        Long tenantId = 1L;
        LocalDate older = LocalDate.of(2026, 1, 10);
        LocalDate newer = LocalDate.of(2026, 5, 20);

        ProductionBatch batch1 = ProductionBatch.builder()
                .id(1L).tenantId(tenantId).productId(10L)
                .quantityKg(BigDecimal.valueOf(50)).batchDate(older).status("COMPLETED").build();
        ProductionBatch batch2 = ProductionBatch.builder()
                .id(2L).tenantId(tenantId).productId(10L)
                .quantityKg(BigDecimal.valueOf(80)).batchDate(newer).status("COMPLETED").build();

        Product product = Product.builder().id(10L).tenantId(tenantId).name("Maní Salado").build();

        when(repository.findByTenantId(tenantId)).thenReturn(List.of(batch1, batch2));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(mapper.toResponse(any(ProductionBatch.class), any())).thenAnswer(invocation -> {
            ProductionBatch b = invocation.getArgument(0);
            ProductionBatchResponseDTO r = new ProductionBatchResponseDTO();
            r.setBatchDate(b.getBatchDate());
            return r;
        });

        List<ProductionBatchResponseDTO> result = service.listByTenant(tenantId);

        assertEquals(2, result.size());
        assertEquals(newer, result.get(0).getBatchDate());
        assertEquals(older, result.get(1).getBatchDate());
    }
}
