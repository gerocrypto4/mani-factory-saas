package com.manifactory.backend.auth.controller;

import com.manifactory.backend.auth.repository.AppUserRepository;
import com.manifactory.backend.auth.jwt.JwtTokenProvider;
import com.manifactory.backend.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final AppUserRepository appUserRepository;

    public AuthController(AuthenticationManager authManager, JwtTokenProvider tokenProvider, AppUserRepository appUserRepository) {
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);

        var user = appUserRepository.findByUsername(req.getUsername())
                .filter(u -> u.isActive())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        Long tenantId = user.getTenantId();
        String role = user.getRole().name();

        String token = tokenProvider.createToken(req.getUsername(), tenantId, role);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @Data
    static class LoginRequest {
        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }

    @Data
    static class LoginResponse {
        private final String token;
    }
}
