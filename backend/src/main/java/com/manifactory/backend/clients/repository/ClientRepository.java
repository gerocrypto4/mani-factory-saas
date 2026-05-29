package com.manifactory.backend.clients.repository;

import com.manifactory.backend.clients.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByTenantId(Long tenantId);
    Optional<Client> findByTenantIdAndId(Long tenantId, Long id);
    Optional<Client> findFirstByTenantIdAndPhone(Long tenantId, String phone);
}
