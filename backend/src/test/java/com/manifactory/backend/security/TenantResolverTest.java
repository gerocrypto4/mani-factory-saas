package com.manifactory.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class TenantResolverTest {

    @Test
    void resolveReturnsExplicitTenantIdForSuperAdmin() {
        TenantContextHolder.setTenantId(42L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("sa", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN"))));
        try {
            assertEquals(12L, TenantResolver.resolve(12L));
        } finally {
            TenantContextHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void resolveUsesContextHolderWhenExplicitTenantIdIsNull() {
        TenantContextHolder.setTenantId(42L);
        try {
            assertEquals(42L, TenantResolver.resolve(null));
        } finally {
            TenantContextHolder.clear();
        }
    }

    @Test
    void resolveThrowsWhenTenantIsMissing() {
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> TenantResolver.resolve(null));
        assertEquals("Tenant ID is required", ex.getMessage());
    }

    @Test
    void resolveThrowsOnTenantMismatchForNonSuperAdmin() {
        TenantContextHolder.setTenantId(42L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        try {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> TenantResolver.resolve(99L));
            assertEquals("Tenant mismatch for authenticated user", ex.getMessage());
        } finally {
            TenantContextHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
