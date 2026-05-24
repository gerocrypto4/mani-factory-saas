package com.manifactory.backend.auth.service;

import com.manifactory.backend.auth.dto.CreateUserDTO;
import com.manifactory.backend.auth.dto.UpdatePasswordDTO;
import com.manifactory.backend.auth.dto.UpdateUserDTO;
import com.manifactory.backend.auth.dto.UserResponseDTO;
import com.manifactory.backend.auth.entity.AppUser;
import com.manifactory.backend.auth.repository.AppUserRepository;
import com.manifactory.backend.exception.NotFoundException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO create(CreateUserDTO dto) {
        if (appUserRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        AppUser user = AppUser.builder()
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .tenantId(dto.getTenantId())
                .role(dto.getRole())
                .active(dto.getActive() == null || dto.getActive())
                .build();

        AppUser saved = appUserRepository.save(user);
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
        return toResponse(saved);
    }

    public UserResponseDTO updatePassword(Long id, UpdatePasswordDTO dto) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        AppUser saved = appUserRepository.save(user);
        return toResponse(saved);
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
