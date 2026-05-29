package com.manifactory.backend.products.bootstrap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "products.bootstrap")
public class ProductBootstrapProperties {

    private boolean enabled = true;
    private List<TenantCatalog> tenants = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<TenantCatalog> getTenants() {
        return tenants;
    }

    public void setTenants(List<TenantCatalog> tenants) {
        this.tenants = tenants;
    }

    public static class TenantCatalog {

        private Long tenantId;
        private boolean replaceExisting = true;
        private List<ProductSeed> products = new ArrayList<>();

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        public boolean isReplaceExisting() {
            return replaceExisting;
        }

        public void setReplaceExisting(boolean replaceExisting) {
            this.replaceExisting = replaceExisting;
        }

        public List<ProductSeed> getProducts() {
            return products;
        }

        public void setProducts(List<ProductSeed> products) {
            this.products = products;
        }
    }

    public static class ProductSeed {

        private String name;
        private String description;
        private BigDecimal price;
        private Double weight = 1.0;
        private String imageUrl;
        private boolean active = true;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
