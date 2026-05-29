package com.manifactory.backend.products.bootstrap;

import com.manifactory.backend.products.entity.Product;
import com.manifactory.backend.products.repository.ProductRepository;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductBootstrapInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductBootstrapInitializer.class);

    private final ProductRepository productRepository;
    private final ProductBootstrapProperties bootstrapProperties;

    public ProductBootstrapInitializer(ProductRepository productRepository,
            ProductBootstrapProperties bootstrapProperties) {
        this.productRepository = productRepository;
        this.bootstrapProperties = bootstrapProperties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!bootstrapProperties.isEnabled()) {
            return;
        }

        List<ProductBootstrapProperties.TenantCatalog> tenants = bootstrapProperties.getTenants();
        if (tenants == null || tenants.isEmpty()) {
            log.info("Product bootstrap skipped because no tenant catalogs were configured.");
            return;
        }

        tenants.stream()
                .filter(tenant -> tenant.getTenantId() != null)
                .forEach(this::seedTenantCatalog);
    }

    protected void seedTenantCatalog(ProductBootstrapProperties.TenantCatalog tenantCatalog) {
        Long tenantId = tenantCatalog.getTenantId();
        List<ProductBootstrapProperties.ProductSeed> seeds = tenantCatalog.getProducts();
        List<Product> existingProducts = productRepository.findAllByTenantId(tenantId).stream()
                .sorted(Comparator.comparing(Product::getId))
                .toList();

        int sharedCount = Math.min(existingProducts.size(), seeds.size());

        for (int i = 0; i < sharedCount; i++) {
            applySeed(existingProducts.get(i), tenantId, seeds.get(i));
            productRepository.save(existingProducts.get(i));
        }

        for (int i = sharedCount; i < seeds.size(); i++) {
            Product created = buildProduct(tenantId, seeds.get(i));
            productRepository.save(created);
        }

        if (tenantCatalog.isReplaceExisting()) {
            for (int i = sharedCount; i < existingProducts.size(); i++) {
                Product leftover = existingProducts.get(i);
                if (leftover.isActive()) {
                    leftover.setActive(false);
                    productRepository.save(leftover);
                }
            }
        }

        log.info("Bootstrap catalog synced for tenant {} with {} configured products.", tenantId, seeds.size());
    }

    private void applySeed(Product target, Long tenantId, ProductBootstrapProperties.ProductSeed seed) {
        target.setTenantId(tenantId);
        target.setName(seed.getName());
        target.setDescription(seed.getDescription());
        target.setPrice(seed.getPrice());
        target.setWeight(seed.getWeight());
        target.setImageUrl(seed.getImageUrl());
        target.setActive(seed.isActive());
    }

    private Product buildProduct(Long tenantId, ProductBootstrapProperties.ProductSeed seed) {
        return Product.builder()
                .tenantId(tenantId)
                .name(seed.getName())
                .description(seed.getDescription())
                .price(seed.getPrice())
                .weight(seed.getWeight())
                .imageUrl(seed.getImageUrl())
                .active(seed.isActive())
                .build();
    }
}
