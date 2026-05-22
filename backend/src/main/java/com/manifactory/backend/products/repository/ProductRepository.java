package com.manifactory.backend.products.repository;

import com.manifactory.backend.products.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByTenantId(Long tenantId);

    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);
}
