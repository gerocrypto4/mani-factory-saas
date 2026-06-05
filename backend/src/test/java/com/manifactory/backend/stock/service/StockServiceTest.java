package com.manifactory.backend.stock.service;

import com.manifactory.backend.products.entity.Product;
import com.manifactory.backend.products.repository.ProductRepository;
import com.manifactory.backend.stock.dto.CreateStockEntryDTO;
import com.manifactory.backend.stock.dto.StockEntryResponseDTO;
import com.manifactory.backend.stock.dto.StockLevelDTO;
import com.manifactory.backend.stock.entity.StockEntry;
import com.manifactory.backend.stock.entity.StockEntryType;
import com.manifactory.backend.stock.mapper.StockEntryMapper;
import com.manifactory.backend.stock.repository.StockEntryRepository;
import com.manifactory.backend.stock.service.impl.StockServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockEntryRepository repository;

    @Mock
    private StockEntryMapper mapper;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private StockServiceImpl service;

    @Test
    void createEntryWithValidProduct_succeeds() {
        Long tenantId = 1L;
        CreateStockEntryDTO dto = new CreateStockEntryDTO();
        dto.setType(StockEntryType.ENTRY);
        dto.setProductId(10L);
        dto.setQuantity(BigDecimal.valueOf(100));
        dto.setEntryDate(LocalDate.now());

        Product product = Product.builder().id(10L).tenantId(tenantId).name("Mani Salado").build();
        StockEntry entity = StockEntry.builder().id(1L).tenantId(tenantId).productId(10L)
                .type(StockEntryType.ENTRY).quantity(BigDecimal.valueOf(100))
                .entryDate(LocalDate.now()).build();
        StockEntryResponseDTO expected = new StockEntryResponseDTO(
                1L, tenantId, 10L, "Mani Salado", StockEntryType.ENTRY,
                BigDecimal.valueOf(100), null, LocalDate.now(), null);

        when(productRepository.findByIdAndTenantId(10L, tenantId)).thenReturn(Optional.of(product));
        when(mapper.toEntity(any(), eq(tenantId))).thenReturn(entity);
        when(repository.save(any())).thenReturn(entity);
        when(mapper.toResponse(any(), eq("Mani Salado"))).thenReturn(expected);

        StockEntryResponseDTO result = service.create(tenantId, dto);

        assertNotNull(result);
        assertEquals("Mani Salado", result.getProductName());
        assertEquals(BigDecimal.valueOf(100), result.getQuantity());
        verify(repository).save(any());
    }

    @Test
    void createEntryWithProductFromOtherTenant_throws() {
        Long tenantId = 1L;
        CreateStockEntryDTO dto = new CreateStockEntryDTO();
        dto.setType(StockEntryType.ENTRY);
        dto.setProductId(99L);
        dto.setQuantity(BigDecimal.valueOf(50));
        dto.setEntryDate(LocalDate.now());

        when(productRepository.findByIdAndTenantId(99L, tenantId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(tenantId, dto));
        assertTrue(ex.getMessage().contains("Product not found"));
    }

    @Test
    void createEntryWithNonexistentProduct_throws() {
        Long tenantId = 1L;
        CreateStockEntryDTO dto = new CreateStockEntryDTO();
        dto.setType(StockEntryType.WITHDRAWAL);
        dto.setProductId(999L);
        dto.setQuantity(BigDecimal.valueOf(10));
        dto.setEntryDate(LocalDate.now());

        when(productRepository.findByIdAndTenantId(999L, tenantId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(tenantId, dto));
        assertTrue(ex.getMessage().contains("Product not found"));
    }

    @Test
    void deleteEntryBelongingToTenant_succeeds() {
        Long tenantId = 1L;
        StockEntry entry = StockEntry.builder()
                .id(5L).tenantId(tenantId).productId(10L)
                .type(StockEntryType.ENTRY).quantity(BigDecimal.valueOf(200))
                .entryDate(LocalDate.now()).build();

        when(repository.findByIdAndTenantId(5L, tenantId)).thenReturn(Optional.of(entry));

        service.delete(tenantId, 5L);

        verify(repository).delete(entry);
    }

    @Test
    void deleteEntryNotBelongingToTenant_throws() {
        when(repository.findByIdAndTenantId(77L, 1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.delete(1L, 77L));
        assertTrue(ex.getMessage().contains("Stock entry not found"));
    }

    @Test
    void currentLevels_calculatesNetQuantity() {
        Long tenantId = 1L;
        Long productId = 10L;
        LocalDate today = LocalDate.now();

        StockEntry entry1 = StockEntry.builder().id(1L).tenantId(tenantId).productId(productId)
                .type(StockEntryType.ENTRY).quantity(BigDecimal.valueOf(100)).entryDate(today).build();
        StockEntry entry2 = StockEntry.builder().id(2L).tenantId(tenantId).productId(productId)
                .type(StockEntryType.ENTRY).quantity(BigDecimal.valueOf(100)).entryDate(today).build();
        StockEntry withdrawal = StockEntry.builder().id(3L).tenantId(tenantId).productId(productId)
                .type(StockEntryType.WITHDRAWAL).quantity(BigDecimal.valueOf(30)).entryDate(today).build();

        Product product = Product.builder().id(productId).tenantId(tenantId).name("Mani Salado").build();

        when(repository.findByTenantId(tenantId)).thenReturn(List.of(entry1, entry2, withdrawal));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        List<StockLevelDTO> levels = service.currentLevels(tenantId);

        assertEquals(1, levels.size());
        assertEquals(new BigDecimal("170"), levels.get(0).getCurrentQuantityKg());
        assertEquals("Mani Salado", levels.get(0).getProductName());
    }
}
