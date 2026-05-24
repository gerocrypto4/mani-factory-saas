package com.manifactory.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TenantResolverTest {

    @Test
    void resolveReturnsExplicitTenantIdWhenProvided() {
        assertEquals(12L, TenantResolver.resolve(12L));
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
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> TenantResolver.resolve(null));
        assertEquals("Tenant ID is required", ex.getMessage());
    }
}
