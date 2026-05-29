package com.manifactory.backend.orders.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "orders.rules")
public class OrderRulesProperties {

    private List<TenantRules> tenants = new ArrayList<>();

    public List<TenantRules> getTenants() {
        return tenants;
    }

    public void setTenants(List<TenantRules> tenants) {
        this.tenants = tenants;
    }

    public Optional<TenantRules> findByTenantId(Long tenantId) {
        if (tenantId == null || tenants == null) {
            return Optional.empty();
        }
        return tenants.stream()
                .filter(rule -> tenantId.equals(rule.getTenantId()))
                .findFirst();
    }

    public static class TenantRules {

        private Long tenantId;
        private Integer minOrderKg = 300;
        private Integer minKgPerFlavor = 10;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        public Integer getMinOrderKg() {
            return minOrderKg;
        }

        public void setMinOrderKg(Integer minOrderKg) {
            this.minOrderKg = minOrderKg;
        }

        public Integer getMinKgPerFlavor() {
            return minKgPerFlavor;
        }

        public void setMinKgPerFlavor(Integer minKgPerFlavor) {
            this.minKgPerFlavor = minKgPerFlavor;
        }
    }
}
