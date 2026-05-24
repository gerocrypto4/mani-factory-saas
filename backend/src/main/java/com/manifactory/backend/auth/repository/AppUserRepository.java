package com.manifactory.backend.auth.repository;

import com.manifactory.backend.auth.entity.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    List<AppUser> findAllByTenantId(Long tenantId);
}
