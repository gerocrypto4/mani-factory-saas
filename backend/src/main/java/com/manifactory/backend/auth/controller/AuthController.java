package com.manifactory.backend.auth.controller;

import com.manifactory.backend.auth.repository.AppUserRepository;
import com.manifactory.backend.auth.dto.ChangeMyPasswordDTO;
import com.manifactory.backend.auth.jwt.JwtTokenProvider;
import com.manifactory.backend.auth.service.AppUserService;
import com.manifactory.backend.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Autenticacion y gestion de credenciales")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;

    public AuthController(AuthenticationManager authManager, JwtTokenProvider tokenProvider,
            AppUserRepository appUserRepository, AppUserService appUserService) {
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.appUserRepository = appUserRepository;
        this.appUserService = appUserService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuario", description = "Autentica usuario y devuelve JWT")
    @SecurityRequirements
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth;
        try {
            auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        } catch (BadCredentialsException ex) {
            log.warn("Login failed for username='{}': bad credentials", req.getUsername());
            throw ex;
        }
        SecurityContextHolder.getContext().setAuthentication(auth);

        var user = appUserRepository.findByUsername(req.getUsername())
                .filter(u -> u.isActive())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        Long tenantId = user.getTenantId();
        String role = user.getRole().name();

        String token = tokenProvider.createToken(req.getUsername(), tenantId, role);
        log.info("Login success for username='{}', tenantId={}, role={}", req.getUsername(), tenantId, role);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Cambiar password propia", description = "Requiere JWT valido")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password actualizada"),
            @ApiResponse(responseCode = "400", description = "Validacion o password actual invalida"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangeMyPasswordDTO dto, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails details) {
            username = details.getUsername();
        } else {
            username = authentication.getName();
        }
        appUserService.changeOwnPassword(username, dto);
        log.info("Password changed for username='{}'", username);
        return ResponseEntity.noContent().build();
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
