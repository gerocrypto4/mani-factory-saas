package com.manifactory.backend.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.manifactory.backend.auth.jwt.JwtTokenProvider;
import com.manifactory.backend.auth.entity.AppUser;
import com.manifactory.backend.auth.entity.AppUserRole;
import com.manifactory.backend.auth.repository.AppUserRepository;
import com.manifactory.backend.auth.service.AppUserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class AuthControllerTest {

    @Test
    void loginProducesValidJwtWithTenantClaim() {
        AuthenticationManager authManager = Mockito.mock(AuthenticationManager.class);
        AppUserRepository userRepository = Mockito.mock(AppUserRepository.class);
        AppUserService appUserService = Mockito.mock(AppUserService.class);
        JwtTokenProvider tokenProvider = new JwtTokenProvider("test-secret-please-change-test-secret-please-change", 3600000L);
        AuthController authController = new AuthController(authManager, tokenProvider, userRepository, appUserService);

        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername("login-user");
        request.setPassword("password");

        Authentication authentication = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        Mockito.when(authManager.authenticate(Mockito.any())).thenReturn(authentication);
        Mockito.when(userRepository.findByUsername("login-user")).thenReturn(Optional.of(AppUser.builder()
                .id(1L)
                .username("login-user")
                .tenantId(88L)
                .role(AppUserRole.ADMIN)
                .passwordHash("$2a$10$test")
                .active(true)
                .build()));

        ResponseEntity<AuthController.LoginResponse> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        String token = response.getBody().getToken();
        assertNotNull(token);
        assertEquals("login-user", tokenProvider.getClaims(token).getSubject());
        assertEquals(88L, ((Number) tokenProvider.getClaims(token).get("tenantId")).longValue());
        assertEquals("ADMIN", tokenProvider.getClaims(token).get("role"));
    }
}
