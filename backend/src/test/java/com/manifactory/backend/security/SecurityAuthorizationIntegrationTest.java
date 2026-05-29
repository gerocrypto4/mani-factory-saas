package com.manifactory.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.manifactory.backend.auth.repository.AppUserRepository;
import com.manifactory.backend.auth.jwt.JwtTokenProvider;
import org.springframework.http.MediaType;
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

    @Autowired
    private AppUserRepository appUserRepository;

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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
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

    @Test
    void clientsEndpointShouldRejectTenantMismatchForNonSuperAdmin() throws Exception {
        String token = tokenProvider.createToken("user@test.com", 1L, "USER");
        mockMvc.perform(get("/api/v1/clients")
                        .param("tenantId", "999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tenant mismatch for authenticated user"));
    }

    @Test
    void ordersEndpointShouldRejectTenantMismatchForNonSuperAdmin() throws Exception {
        String token = tokenProvider.createToken("user@test.com", 1L, "USER");
        mockMvc.perform(get("/api/v1/orders")
                        .param("tenantId", "999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tenant mismatch for authenticated user"));
    }

    @Test
    void productsEndpointShouldAllowTenantOverrideForSuperAdmin() throws Exception {
        String token = tokenProvider.createToken("superadmin@test.com", 1L, "SUPERADMIN");
        mockMvc.perform(get("/api/v1/products")
                        .param("tenantId", "999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void changePasswordEndpointShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"old\",\"newPassword\":\"NewStrong#123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void openApiDocsShouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void publicCatalogShouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/public/catalog/products")
                        .param("tenantId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void createUserShouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u_noauth\",\"password\":\"clave1234\",\"tenantId\":1,\"role\":\"USER\",\"active\":true}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void createUserShouldReturnForbiddenForUserRole() throws Exception {
        String token = tokenProvider.createToken("user@test.com", 1L, "USER");
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u_forbidden\",\"password\":\"clave1234\",\"tenantId\":1,\"role\":\"USER\",\"active\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUserShouldReturnCreatedForSuperAdmin() throws Exception {
        String uniqueUsername = "u_created_ok_" + System.nanoTime();
        String token = tokenProvider.createToken("superadmin@test.com", 1L, "SUPERADMIN");
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + uniqueUsername + "\",\"password\":\"clave1234\",\"tenantId\":1,\"role\":\"USER\",\"active\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(uniqueUsername));
    }

    @Test
    void updateAndUpdatePasswordShouldCoverUnauthorizedForbiddenAndSuccess() throws Exception {
        String uniqueUsername = "u_update_target_" + System.nanoTime();
        String superadminToken = tokenProvider.createToken("superadmin@test.com", 1L, "SUPERADMIN");
        String userToken = tokenProvider.createToken("user@test.com", 1L, "USER");

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + superadminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + uniqueUsername + "\",\"password\":\"clave1234\",\"tenantId\":1,\"role\":\"USER\",\"active\":true}"))
                .andExpect(status().isCreated())
                .andReturn();

        long userId = appUserRepository.findByUsername(uniqueUsername)
                .orElseThrow()
                .getId();

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":1,\"role\":\"ADMIN\",\"active\":true}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":1,\"role\":\"ADMIN\",\"active\":true}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .header("Authorization", "Bearer " + superadminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenantId\":1,\"role\":\"ADMIN\",\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(put("/api/v1/users/{id}/password", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"nuevo1234\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/users/{id}/password", userId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"nuevo1234\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/users/{id}/password", userId)
                        .header("Authorization", "Bearer " + superadminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"nuevo1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }
}
