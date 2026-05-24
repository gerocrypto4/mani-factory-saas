package com.manifactory.backend.security;

public final class TenantResolver {

    private TenantResolver() {
    }

    public static Long resolve(Long explicitTenantId) {
        if (explicitTenantId != null) {
            return explicitTenantId;
        }

        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required");
        }

        return tenantId;
    }
}
