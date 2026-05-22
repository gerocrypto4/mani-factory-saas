package com.manifactory.backend.security;

public final class TenantContextHolder {

    private static final ThreadLocal<Long> TENANT = new ThreadLocal<>();

    private TenantContextHolder() {}

    public static void setTenantId(Long tenantId) {
        TENANT.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT.get();
    }

    public static void clear() {
        TENANT.remove();
    }
}
