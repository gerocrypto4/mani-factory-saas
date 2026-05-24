package com.manifactory.backend.orders.repository;

import com.manifactory.backend.orders.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByTenantId(Long tenantId);

    Optional<Order> findByIdAndTenantId(Long id, Long tenantId);
}
