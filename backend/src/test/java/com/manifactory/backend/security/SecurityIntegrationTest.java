package com.manifactory.backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manifactory.backend.auth.jwt.JwtAuthenticationFilter;
import com.manifactory.backend.auth.jwt.JwtTokenProvider;
import com.manifactory.backend.clients.controller.ClientController;
import com.manifactory.backend.clients.dto.ClientResponseDTO;
import com.manifactory.backend.clients.dto.CreateClientDTO;
import com.manifactory.backend.clients.service.ClientService;
import com.manifactory.backend.exception.GlobalExceptionHandler;
import com.manifactory.backend.exception.NotFoundException;
import com.manifactory.backend.orders.controller.OrderController;
import com.manifactory.backend.orders.dto.CreateOrderDTO;
import com.manifactory.backend.orders.dto.CreateOrderItemDTO;
import com.manifactory.backend.orders.dto.OrderResponseDTO;
import com.manifactory.backend.orders.service.OrderService;
import com.manifactory.backend.products.controller.ProductController;
import com.manifactory.backend.products.dto.ProductResponseDTO;
import com.manifactory.backend.products.service.ProductService;
import java.math.BigDecimal;
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
    private ObjectMapper objectMapper;
    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        productService = Mockito.mock(ProductService.class);
        orderService = Mockito.mock(OrderService.class);
        clientService = Mockito.mock(ClientService.class);
        objectMapper = new ObjectMapper();
        tokenProvider = new JwtTokenProvider("test-secret-please-change-test-secret-please-change", 3600000L);
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(tokenProvider);

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ProductController(productService),
                        new OrderController(orderService),
                        new ClientController(clientService))
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
        item.setUnitPrice(BigDecimal.valueOf(25.00));
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
}
