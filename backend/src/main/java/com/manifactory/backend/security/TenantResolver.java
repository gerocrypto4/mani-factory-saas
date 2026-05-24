package com.manifactory.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class TenantResolver {

    private TenantResolver() {
    }

    public static Long resolve(Long explicitTenantId) {
        Long contextTenantId = TenantContextHolder.getTenantId();
        boolean superAdmin = isSuperAdmin();

        if (contextTenantId == null) {
            if (explicitTenantId != null && superAdmin) {
                return explicitTenantId;
            }
            throw new IllegalArgumentException("Tenant ID is required");
        }

        if (explicitTenantId != null) {
            if (superAdmin) {
                return explicitTenantId;
            }
            if (!contextTenantId.equals(explicitTenantId)) {
                throw new IllegalArgumentException("Tenant mismatch for authenticated user");
            }
        }

        return contextTenantId;
    }

    private static boolean isSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPERADMIN".equals(authority.getAuthority()));
    }
}
