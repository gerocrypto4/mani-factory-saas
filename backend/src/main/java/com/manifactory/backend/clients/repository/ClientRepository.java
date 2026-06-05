package com.manifactory.backend.clients.repository;

import com.manifactory.backend.clients.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByTenantId(Long tenantId);
    Optional<Client> findByTenantIdAndId(Long tenantId, Long id);
    Optional<Client> findFirstByTenantIdAndPhone(Long tenantId, String phone);
    Optional<Client> findFirstByTenantIdAndEmailIgnoreCase(Long tenantId, String email);

    @Query(
            value = """
                    select c from Client c
                    where c.tenantId = :tenantId
                      and (
                        :query is null or :query = ''
                        or lower(c.name) like lower(concat('%', :query, '%'))
                        or lower(coalesce(c.businessName, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(c.email, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(c.phone, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(c.city, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(c.preferredTransport, '')) like lower(concat('%', :query, '%'))
                      )
                    """,
            countQuery = """
                    select count(c) from Client c
                    where c.tenantId = :tenantId
                      and (
                        :query is null or :query = ''
                        or lower(c.name) like lower(concat('%', :query, '%'))
                        or lower(coalesce(c.businessName, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(c.email, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(c.phone, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(c.city, '')) like lower(concat('%', :query, '%'))
                        or lower(coalesce(c.preferredTransport, '')) like lower(concat('%', :query, '%'))
                      )
                    """
    )
    Page<Client> searchByTenantId(Long tenantId, String query, Pageable pageable);
}
