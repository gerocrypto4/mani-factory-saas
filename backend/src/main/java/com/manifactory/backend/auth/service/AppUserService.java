package com.manifactory.backend.auth.service;

import com.manifactory.backend.auth.dto.CreateUserDTO;
import com.manifactory.backend.auth.dto.ChangeMyPasswordDTO;
import com.manifactory.backend.auth.dto.UpdatePasswordDTO;
import com.manifactory.backend.auth.dto.UpdateUserDTO;
import com.manifactory.backend.auth.dto.UserResponseDTO;
import com.manifactory.backend.auth.entity.AppUser;
import com.manifactory.backend.auth.repository.AppUserRepository;
import com.manifactory.backend.exception.NotFoundException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppUserService {

    private static final Logger log = LoggerFactory.getLogger(AppUserService.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder,
            PasswordPolicyService passwordPolicyService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyService = passwordPolicyService;
    }

    public UserResponseDTO create(CreateUserDTO dto) {
        if (appUserRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        passwordPolicyService.validateOrThrow(dto.getPassword());

        AppUser user = AppUser.builder()
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .tenantId(dto.getTenantId())
                .role(dto.getRole())
                .active(dto.getActive() == null || dto.getActive())
                .build();

        AppUser saved = appUserRepository.save(user);
        log.info("User created: username='{}', tenantId={}, role={}", saved.getUsername(), saved.getTenantId(), saved.getRole());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> list(Long tenantId) {
        List<AppUser> users = tenantId == null
                ? appUserRepository.findAll()
                : appUserRepository.findAllByTenantId(tenantId);
        return users.stream().map(this::toResponse).toList();
    }

    public UserResponseDTO update(Long id, UpdateUserDTO dto) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setTenantId(dto.getTenantId());
        user.setRole(dto.getRole());
        user.setActive(dto.getActive());

        AppUser saved = appUserRepository.save(user);
        log.info("User updated: id={}, tenantId={}, role={}, active={}", saved.getId(), saved.getTenantId(), saved.getRole(), saved.isActive());
        return toResponse(saved);
    }

    public UserResponseDTO updatePassword(Long id, UpdatePasswordDTO dto) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        passwordPolicyService.validateOrThrow(dto.getPassword());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        AppUser saved = appUserRepository.save(user);
        log.info("Password updated by admin for userId={}", saved.getId());
        return toResponse(saved);
    }

    public void changeOwnPassword(String username, ChangeMyPasswordDTO dto) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            log.warn("Password change failed for username='{}': wrong current password", username);
            throw new IllegalArgumentException("Current password is incorrect");
        }
        passwordPolicyService.validateOrThrow(dto.getNewPassword());
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        appUserRepository.save(user);
        log.info("Password changed by authenticated user username='{}'", username);
    }

    private UserResponseDTO toResponse(AppUser user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getTenantId(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
