package com.manifactory.backend.finance.service;

import com.manifactory.backend.finance.dto.CreateFinanceEntryDTO;
import com.manifactory.backend.finance.dto.FinanceEntryResponseDTO;
import com.manifactory.backend.finance.dto.FinanceSummaryDTO;
import com.manifactory.backend.finance.entity.FinanceEntry;
import com.manifactory.backend.finance.entity.FinanceEntryType;
import com.manifactory.backend.finance.mapper.FinanceEntryMapper;
import com.manifactory.backend.finance.repository.FinanceEntryRepository;
import com.manifactory.backend.finance.service.impl.FinanceServiceImpl;
import com.manifactory.backend.orders.entity.Order;
import com.manifactory.backend.orders.entity.OrderStatus;
import com.manifactory.backend.orders.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTest {

    @Mock
    private FinanceEntryRepository repository;

    @Mock
    private FinanceEntryMapper mapper;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private FinanceServiceImpl service;

    @Test
    void createIncomeWithValidCategory() {
        CreateFinanceEntryDTO dto = new CreateFinanceEntryDTO();
        dto.setType(FinanceEntryType.INCOME);
        dto.setCategory("cobranzas");
        dto.setAmount(BigDecimal.valueOf(1500));
        dto.setEntryDate(LocalDate.now());

        FinanceEntry entity = FinanceEntry.builder()
                .id(1L).tenantId(1L).type(FinanceEntryType.INCOME)
                .category("cobranzas").amount(BigDecimal.valueOf(1500))
                .entryDate(LocalDate.now()).build();

        FinanceEntryResponseDTO expected = new FinanceEntryResponseDTO(
                1L, 1L, FinanceEntryType.INCOME, "cobranzas", null,
                BigDecimal.valueOf(1500), LocalDate.now(), null, null);

        when(mapper.toEntity(any(), any())).thenReturn(entity);
        when(repository.save(any())).thenReturn(entity);
        when(mapper.toResponse(any())).thenReturn(expected);

        FinanceEntryResponseDTO result = service.create(1L, dto);

        assertNotNull(result);
        assertEquals(FinanceEntryType.INCOME, result.getType());
        assertEquals("cobranzas", result.getCategory());
        assertEquals(BigDecimal.valueOf(1500), result.getAmount());
        verify(repository).save(any());
    }

    @Test
    void createRejectsInvalidCategoryForType() {
        CreateFinanceEntryDTO dto = new CreateFinanceEntryDTO();
        dto.setType(FinanceEntryType.INCOME);
        dto.setCategory("insumos"); // valid EXPENSE category, invalid for INCOME
        dto.setAmount(BigDecimal.valueOf(500));
        dto.setEntryDate(LocalDate.now());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(1L, dto));
        assertTrue(ex.getMessage().contains("Invalid category"));
    }

    @Test
    void deleteEntryBelongingToTenant() {
        FinanceEntry entry = FinanceEntry.builder()
                .id(5L).tenantId(1L).type(FinanceEntryType.EXPENSE)
                .category("sueldos").amount(BigDecimal.valueOf(20000))
                .entryDate(LocalDate.now()).build();

        when(repository.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(entry));

        service.delete(1L, 5L);

        verify(repository).delete(entry);
    }

    @Test
    void deleteRejectsEntryNotBelongingToTenant() {
        when(repository.findByIdAndTenantId(99L, 1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.delete(1L, 99L));
        assertTrue(ex.getMessage().contains("Finance entry not found"));
    }

    @Test
    void summaryIncludesOnlyRecognizedOrderStatuses() {
        Long tenantId = 1L;
        YearMonth period = YearMonth.of(2026, 6);
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();

        OffsetDateTime inPeriod = OffsetDateTime.parse("2026-06-10T10:00:00+00:00");
        OffsetDateTime outOfPeriod = OffsetDateTime.parse("2026-05-10T10:00:00+00:00");

        // CONFIRMED + SHIPPED + DELIVERED in period → should be summed (1000+2000+3000=6000)
        Order confirmed = Order.builder().id(1L).tenantId(tenantId).clientId(1L)
                .status(OrderStatus.CONFIRMED).total(BigDecimal.valueOf(1000))
                .createdAt(inPeriod).build();
        Order shipped = Order.builder().id(2L).tenantId(tenantId).clientId(1L)
                .status(OrderStatus.SHIPPED).total(BigDecimal.valueOf(2000))
                .createdAt(inPeriod).build();
        Order delivered = Order.builder().id(3L).tenantId(tenantId).clientId(1L)
                .status(OrderStatus.DELIVERED).total(BigDecimal.valueOf(3000))
                .createdAt(inPeriod).build();
        // PENDING and CANCELLED in period → must be ignored
        Order pending = Order.builder().id(4L).tenantId(tenantId).clientId(1L)
                .status(OrderStatus.PENDING).total(BigDecimal.valueOf(9999))
                .createdAt(inPeriod).build();
        Order cancelled = Order.builder().id(5L).tenantId(tenantId).clientId(1L)
                .status(OrderStatus.CANCELLED).total(BigDecimal.valueOf(9999))
                .createdAt(inPeriod).build();
        // CONFIRMED but outside the period → must be ignored
        Order oldConfirmed = Order.builder().id(6L).tenantId(tenantId).clientId(1L)
                .status(OrderStatus.CONFIRMED).total(BigDecimal.valueOf(9999))
                .createdAt(outOfPeriod).build();

        when(orderRepository.findByTenantId(tenantId))
                .thenReturn(List.of(confirmed, shipped, delivered, pending, cancelled, oldConfirmed));
        when(repository.findByTenantIdAndEntryDateBetween(tenantId, start, end))
                .thenReturn(List.of());

        FinanceSummaryDTO summary = service.summary(tenantId, period);

        assertEquals(new BigDecimal("6000"), summary.getSystemSales());
        assertEquals(3L, summary.getSalesOrdersCount());
        assertEquals(BigDecimal.ZERO, summary.getManualIncome());
        assertEquals(BigDecimal.ZERO, summary.getExpenses());
        assertEquals(new BigDecimal("6000"), summary.getTotalIncome());
        assertEquals(new BigDecimal("6000"), summary.getNetResult());
    }
}
