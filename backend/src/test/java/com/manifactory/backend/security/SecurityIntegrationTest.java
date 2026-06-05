package com.manifactory.backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.manifactory.backend.auth.jwt.JwtAuthenticationFilter;
import com.manifactory.backend.auth.jwt.JwtTokenProvider;
import com.manifactory.backend.clients.controller.ClientController;
import com.manifactory.backend.clients.dto.ClientPageResponseDTO;
import com.manifactory.backend.clients.dto.ClientOrderHistoryItemDTO;
import com.manifactory.backend.clients.dto.ClientOrderHistoryResponseDTO;
import com.manifactory.backend.clients.dto.ClientResponseDTO;
import com.manifactory.backend.clients.dto.CreateClientDTO;
import com.manifactory.backend.clients.service.ClientService;
import com.manifactory.backend.exception.GlobalExceptionHandler;
import com.manifactory.backend.exception.NotFoundException;
import com.manifactory.backend.finance.controller.FinanceController;
import com.manifactory.backend.finance.dto.CreateFinanceEntryDTO;
import com.manifactory.backend.finance.dto.FinanceEntryResponseDTO;
import com.manifactory.backend.finance.entity.FinanceEntryType;
import com.manifactory.backend.finance.service.FinanceService;
import com.manifactory.backend.stock.controller.StockController;
import com.manifactory.backend.stock.dto.StockEntryResponseDTO;
import com.manifactory.backend.stock.entity.StockEntryType;
import com.manifactory.backend.stock.service.StockService;
import com.manifactory.backend.production.controller.ProductionBatchController;
import com.manifactory.backend.production.dto.CreateProductionBatchDTO;
import com.manifactory.backend.production.dto.ProductionBatchResponseDTO;
import com.manifactory.backend.production.service.ProductionBatchService;
import com.manifactory.backend.orders.controller.OrderController;
import com.manifactory.backend.orders.dto.CreateOrderDTO;
import com.manifactory.backend.orders.dto.CreateOrderItemDTO;
import com.manifactory.backend.orders.dto.OrderResponseDTO;
import com.manifactory.backend.orders.entity.OrderStatus;
import com.manifactory.backend.orders.service.OrderService;
import com.manifactory.backend.products.controller.ProductController;
import com.manifactory.backend.products.dto.ProductResponseDTO;
import com.manifactory.backend.products.service.ProductService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SecurityIntegrationTest {

    private MockMvc mockMvc;
    private ProductService productService;
    private OrderService orderService;
    private ClientService clientService;
    private FinanceService financeService;
    private StockService stockService;
    private ProductionBatchService productionBatchService;
    private ObjectMapper objectMapper;
    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        productService = Mockito.mock(ProductService.class);
        orderService = Mockito.mock(OrderService.class);
        clientService = Mockito.mock(ClientService.class);
        financeService = Mockito.mock(FinanceService.class);
        stockService = Mockito.mock(StockService.class);
        productionBatchService = Mockito.mock(ProductionBatchService.class);
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        tokenProvider = new JwtTokenProvider("test-secret-please-change-test-secret-please-change", 3600000L);
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(tokenProvider);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ProductController(productService),
                        new OrderController(orderService),
                        new ClientController(clientService),
                        new FinanceController(financeService),
                        new StockController(stockService),
                        new ProductionBatchController(productionBatchService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilter(jwtFilter, "/*")
                .build();
    }

    @Test
    void shouldUseTenantFromJwtForProductList() throws Exception {
        Long tenantId = 101L;
        String token = tokenProvider.createToken("user@example.com", tenantId, "USER");

        ProductResponseDTO product = new ProductResponseDTO();
        product.setId(1L);
        product.setTenantId(tenantId);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(99.99));

        Mockito.when(productService.listByTenant(eq(tenantId))).thenReturn(List.of(product));

        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tenantId").value(tenantId.intValue()))
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        Mockito.verify(productService).listByTenant(eq(tenantId));
    }

    @Test
    void shouldUseTenantFromJwtForOrderCreation() throws Exception {
        Long tenantId = 55L;
        String token = tokenProvider.createToken("order-user", tenantId, "USER");

        CreateOrderDTO createOrder = new CreateOrderDTO();
        createOrder.setClientId(22L);
        CreateOrderItemDTO item = new CreateOrderItemDTO();
        item.setProductId(5L);
        item.setQuantity(2);
        createOrder.setItems(List.of(item));

        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(10L);
        response.setTenantId(tenantId);
        response.setClientId(22L);
        response.setStatus(com.manifactory.backend.orders.entity.OrderStatus.PENDING);
        response.setTotal(BigDecimal.valueOf(50.00));

        Mockito.when(orderService.create(eq(tenantId), any(CreateOrderDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrder))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(tenantId.intValue()))
                .andExpect(jsonPath("$.clientId").value(22))
                .andExpect(jsonPath("$.total").value(50.00));

        Mockito.verify(orderService).create(eq(tenantId), any(CreateOrderDTO.class));
    }

    @Test
    void shouldUseTenantFromJwtForClientCreate() throws Exception {
        Long tenantId = 17L;
        String token = tokenProvider.createToken("client-user", tenantId, "USER");

        CreateClientDTO createClient = new CreateClientDTO();
        createClient.setName("Test Client");
        createClient.setEmail("client@example.com");

        ClientResponseDTO response = new ClientResponseDTO(
                2L, tenantId, "Test Client", "Business", "client@example.com", "+123456789", "City", "Fast", null, null);

        Mockito.when(clientService.create(eq(tenantId), any(CreateClientDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createClient))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(tenantId.intValue()))
                .andExpect(jsonPath("$.name").value("Test Client"));

        Mockito.verify(clientService).create(eq(tenantId), any(CreateClientDTO.class));
    }

    @Test
    void shouldUseTenantFromJwtForClientSearch() throws Exception {
        Long tenantId = 18L;
        String token = tokenProvider.createToken("client-user", tenantId, "USER");

        ClientResponseDTO client = new ClientResponseDTO(
                3L, tenantId, "Client Search", "Business", "search@example.com", "3000", "City", "Retiro", null, null);
        ClientPageResponseDTO response = new ClientPageResponseDTO(List.of(client), 0, 6, 1, 1, false, false, "client");

        Mockito.when(clientService.searchByTenant(eq(tenantId), eq("client"), eq(0), eq(6))).thenReturn(response);

        mockMvc.perform(get("/api/v1/clients/search")
                        .param("q", "client")
                        .param("page", "0")
                        .param("size", "6")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Client Search"));

        Mockito.verify(clientService).searchByTenant(eq(tenantId), eq("client"), eq(0), eq(6));
    }

    @Test
    void shouldUseTenantFromJwtForClientHistory() throws Exception {
        Long tenantId = 19L;
        String token = tokenProvider.createToken("client-user", tenantId, "USER");

        ClientOrderHistoryItemDTO latest = new ClientOrderHistoryItemDTO(
                11L, OrderStatus.CONFIRMED, BigDecimal.valueOf(12000), 3, 30,
                java.time.OffsetDateTime.now(), java.time.OffsetDateTime.now());
        ClientOrderHistoryResponseDTO response = new ClientOrderHistoryResponseDTO(7L, "Client History", 2L, latest, List.of(latest));

        Mockito.when(clientService.getOrderHistory(eq(tenantId), eq(7L), eq(3))).thenReturn(response);

        mockMvc.perform(get("/api/v1/clients/7/orders")
                        .param("limit", "3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(7))
                .andExpect(jsonPath("$.totalOrders").value(2));

        Mockito.verify(clientService).getOrderHistory(eq(tenantId), eq(7L), eq(3));
    }

    @Test
    void shouldReturnStandardNotFoundError() throws Exception {
        Long tenantId = 4L;
        String token = tokenProvider.createToken("user@example.com", tenantId, "USER");

        Mockito.when(productService.getById(eq(tenantId), eq(99L)))
                .thenThrow(new NotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/products/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NotFoundException"))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    // Note: standalone MockMvc has no Spring Security filter chain enforcing 401.
    // Without a valid JWT, TenantResolver.resolve() throws IllegalArgumentException
    // ("Tenant ID is required") → GlobalExceptionHandler → 400 BAD_REQUEST.
    @Test
    void financeCreateWithoutAuthIsRejected() throws Exception {
        String body = "{\"type\":\"INCOME\",\"amount\":500,\"category\":\"cobranzas\",\"entryDate\":\"2026-01-15\"}";

        mockMvc.perform(post("/api/v1/finances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // Note: same pattern as Finance — TenantResolver.resolve(null) throws
    // IllegalArgumentException → GlobalExceptionHandler → 400 BAD_REQUEST.
    @Test
    void stockCreateWithoutAuth_returns400() throws Exception {
        String body = "{\"type\":\"ENTRY\",\"productId\":5,\"quantity\":100,\"entryDate\":\"2026-01-20\"}";

        mockMvc.perform(post("/api/v1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stockCreateWithValidJwt_callsServiceAndReturns201() throws Exception {
        Long tenantId = 33L;
        String token = tokenProvider.createToken("stock-user", tenantId, "USER");

        String body = "{\"type\":\"ENTRY\",\"productId\":5,\"quantity\":100,\"entryDate\":\"2026-01-20\"}";

        StockEntryResponseDTO response = new StockEntryResponseDTO(
                20L, tenantId, 5L, "Mani Salado", StockEntryType.ENTRY,
                java.math.BigDecimal.valueOf(100), null, LocalDate.of(2026, 1, 20), null);

        Mockito.when(stockService.create(eq(tenantId), any(com.manifactory.backend.stock.dto.CreateStockEntryDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(tenantId.intValue()))
                .andExpect(jsonPath("$.productName").value("Mani Salado"));

        Mockito.verify(stockService).create(eq(tenantId), any(com.manifactory.backend.stock.dto.CreateStockEntryDTO.class));
    }

    @Test
    void productionCreateWithoutAuth_returns400() throws Exception {
        String body = "{\"productId\":5,\"quantityKg\":100,\"batchDate\":\"2026-01-20\"}";

        mockMvc.perform(post("/api/v1/production")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void productionCreateWithValidJwt_returns201() throws Exception {
        Long tenantId = 44L;
        String token = tokenProvider.createToken("prod-user", tenantId, "USER");

        String body = "{\"productId\":5,\"quantityKg\":100,\"batchDate\":\"2026-01-20\"}";

        ProductionBatchResponseDTO response = new ProductionBatchResponseDTO(
                1L, tenantId, 5L, "Maní Salado", java.math.BigDecimal.valueOf(100),
                LocalDate.of(2026, 1, 20), null, "COMPLETED", null);

        Mockito.when(productionBatchService.create(eq(tenantId), any(CreateProductionBatchDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/production")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(tenantId.intValue()))
                .andExpect(jsonPath("$.productName").value("Maní Salado"));

        Mockito.verify(productionBatchService).create(eq(tenantId), any(CreateProductionBatchDTO.class));
    }

    @Test
    void financeCreateWithValidJwtCallsServiceAndReturns201() throws Exception {
        Long tenantId = 42L;
        String token = tokenProvider.createToken("finance-user", tenantId, "USER");

        String body = "{\"type\":\"INCOME\",\"amount\":500,\"category\":\"cobranzas\",\"entryDate\":\"2026-01-15\"}";

        FinanceEntryResponseDTO response = new FinanceEntryResponseDTO(
                10L, tenantId, FinanceEntryType.INCOME, "cobranzas", null,
                java.math.BigDecimal.valueOf(500), LocalDate.of(2026, 1, 15), null, null);

        Mockito.when(financeService.create(eq(tenantId), any(CreateFinanceEntryDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/finances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value(tenantId.intValue()))
                .andExpect(jsonPath("$.category").value("cobranzas"));

        Mockito.verify(financeService).create(eq(tenantId), any(CreateFinanceEntryDTO.class));
    }
}
