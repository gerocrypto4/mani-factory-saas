package com.manifactory.backend.finance.service.impl;

import com.manifactory.backend.finance.dto.CreateFinanceEntryDTO;
import com.manifactory.backend.finance.dto.FinanceCategoryTotalDTO;
import com.manifactory.backend.finance.dto.FinanceEntryResponseDTO;
import com.manifactory.backend.finance.dto.FinanceSummaryDTO;
import com.manifactory.backend.finance.entity.FinanceEntry;
import com.manifactory.backend.finance.entity.FinanceEntryType;
import com.manifactory.backend.finance.mapper.FinanceEntryMapper;
import com.manifactory.backend.finance.repository.FinanceEntryRepository;
import com.manifactory.backend.finance.service.FinanceService;
import com.manifactory.backend.orders.entity.Order;
import com.manifactory.backend.orders.entity.OrderStatus;
import com.manifactory.backend.orders.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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
public class FinanceServiceImpl implements FinanceService {

    private static final List<String> INCOME_CATEGORIES = List.of("cobranzas", "ajustes", "otros ingresos");
    private static final List<String> EXPENSE_CATEGORIES = List.of("insumos", "sueldos", "alquileres", "transporte", "otros gastos");
    private static final List<OrderStatus> RECOGNIZED_ORDER_STATUSES = List.of(
            OrderStatus.CONFIRMED,
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED
    );

    private final FinanceEntryRepository repository;
    private final FinanceEntryMapper mapper;
    private final OrderRepository orderRepository;

    @Override
    public FinanceEntryResponseDTO create(Long tenantId, CreateFinanceEntryDTO dto) {
        validateCategory(dto.getType(), dto.getCategory());
        FinanceEntry entity = mapper.toEntity(dto, tenantId);
        FinanceEntry saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceEntryResponseDTO> listByTenant(Long tenantId, YearMonth period) {
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();
        return repository.findByTenantIdAndEntryDateBetween(tenantId, start, end).stream()
                .sorted(this::compareEntriesDesc)
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceSummaryDTO summary(Long tenantId, YearMonth period) {
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();

        List<Order> tenantOrders = orderRepository.findByTenantId(tenantId);
        BigDecimal systemSales = tenantOrders.stream()
                .filter(order -> order.getCreatedAt() != null)
                .filter(order -> isCurrentMonth(order.getCreatedAt().toLocalDate(), period))
                .filter(order -> RECOGNIZED_ORDER_STATUSES.contains(order.getStatus()))
                .map(order -> order.getTotal() == null ? BigDecimal.ZERO : order.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long salesOrdersCount = tenantOrders.stream()
                .filter(order -> order.getCreatedAt() != null)
                .filter(order -> isCurrentMonth(order.getCreatedAt().toLocalDate(), period))
                .filter(order -> RECOGNIZED_ORDER_STATUSES.contains(order.getStatus()))
                .count();

        List<FinanceEntry> monthEntries = repository.findByTenantIdAndEntryDateBetween(tenantId, start, end);
        BigDecimal manualIncome = monthEntries.stream()
                .filter(entry -> entry.getType() == FinanceEntryType.INCOME)
                .map(entry -> entry.getAmount() == null ? BigDecimal.ZERO : entry.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenses = monthEntries.stream()
                .filter(entry -> entry.getType() == FinanceEntryType.EXPENSE)
                .map(entry -> entry.getAmount() == null ? BigDecimal.ZERO : entry.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> expenseTotals = monthEntries.stream()
                .filter(entry -> entry.getType() == FinanceEntryType.EXPENSE)
                .collect(Collectors.groupingBy(
                        entry -> normalizeCategory(entry.getCategory(), EXPENSE_CATEGORIES),
                        Collectors.mapping(
                                entry -> entry.getAmount() == null ? BigDecimal.ZERO : entry.getAmount(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )));

        List<FinanceCategoryTotalDTO> expenseByCategory = new ArrayList<>();
        for (String category : EXPENSE_CATEGORIES) {
            expenseByCategory.add(new FinanceCategoryTotalDTO(category, expenseTotals.getOrDefault(category, BigDecimal.ZERO)));
        }

        BigDecimal totalIncome = systemSales.add(manualIncome);
        BigDecimal netResult = totalIncome.subtract(expenses);

        return new FinanceSummaryDTO(
                systemSales,
                salesOrdersCount,
                manualIncome,
                expenses,
                totalIncome,
                netResult,
                expenseByCategory
        );
    }

    @Override
    public void delete(Long tenantId, Long id) {
        FinanceEntry entry = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Finance entry not found"));
        repository.delete(entry);
    }

    private void validateCategory(FinanceEntryType type, String category) {
        String normalized = normalize(category);
        List<String> allowed = type == FinanceEntryType.INCOME ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
        if (normalized == null || allowed.stream().noneMatch(candidate -> candidate.equalsIgnoreCase(normalized))) {
            throw new IllegalArgumentException("Invalid category for finance entry type");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim().toLowerCase();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeCategory(String category, List<String> allowed) {
        String normalized = normalize(category);
        return allowed.stream()
                .filter(candidate -> candidate.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(normalized);
    }

    private int compareEntriesDesc(FinanceEntry left, FinanceEntry right) {
        int byDate = compareDatesDesc(left.getEntryDate(), right.getEntryDate());
        if (byDate != 0) {
            return byDate;
        }
        return compareInstantsDesc(left.getCreatedAt(), right.getCreatedAt());
    }

    private int compareDatesDesc(LocalDate left, LocalDate right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return right.compareTo(left);
    }

    private int compareInstantsDesc(java.time.OffsetDateTime left, java.time.OffsetDateTime right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return right.compareTo(left);
    }

    private boolean isCurrentMonth(LocalDate value, YearMonth month) {
        return value != null && YearMonth.from(value).equals(month);
    }
}
