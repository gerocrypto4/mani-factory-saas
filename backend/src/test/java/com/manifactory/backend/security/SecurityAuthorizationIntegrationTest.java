package com.manifactory.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.manifactory.backend.auth.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
class SecurityAuthorizationIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void tenantsEndpointShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/tenants"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void tenantsEndpointShouldReturnForbiddenForUserRole() throws Exception {
        String token = tokenProvider.createToken("user@test.com", 1L, "USER");
        mockMvc.perform(get("/api/v1/tenants")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void tenantsEndpointShouldAllowSuperAdmin() throws Exception {
        String token = tokenProvider.createToken("superadmin@test.com", 1L, "SUPERADMIN");
        mockMvc.perform(get("/api/v1/tenants")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void usersEndpointShouldReturnForbiddenForUserRole() throws Exception {
        String token = tokenProvider.createToken("user@test.com", 1L, "USER");
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void usersEndpointShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void productsEndpointShouldRejectTenantMismatchForNonSuperAdmin() throws Exception {
        String token = tokenProvider.createToken("user@test.com", 1L, "USER");
        mockMvc.perform(get("/api/v1/products")
                        .param("tenantId", "999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tenant mismatch for authenticated user"));
    }
}
