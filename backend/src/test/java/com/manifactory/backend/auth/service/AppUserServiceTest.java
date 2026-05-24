package com.manifactory.backend.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.manifactory.backend.auth.dto.CreateUserDTO;
import com.manifactory.backend.auth.dto.UpdatePasswordDTO;
import com.manifactory.backend.auth.dto.UpdateUserDTO;
import com.manifactory.backend.auth.entity.AppUser;
import com.manifactory.backend.auth.entity.AppUserRole;
import com.manifactory.backend.auth.repository.AppUserRepository;
import com.manifactory.backend.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService service;

    @Test
    void createUser() {
        CreateUserDTO dto = new CreateUserDTO();
        dto.setUsername("u1");
        dto.setPassword("secret");
        dto.setTenantId(1L);
        dto.setRole(AppUserRole.ADMIN);
        dto.setActive(true);

        when(repository.existsByUsername("u1")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hash");
        when(repository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        var created = service.create(dto);
        assertNotNull(created);
        assertEquals(10L, created.getId());
        assertEquals("u1", created.getUsername());
        assertEquals(1L, created.getTenantId());
    }

    @Test
    void updateUserNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setTenantId(1L);
        dto.setRole(AppUserRole.USER);
        dto.setActive(true);

        assertThrows(NotFoundException.class, () -> service.update(99L, dto));
    }

    @Test
    void updatePassword() {
        AppUser user = AppUser.builder()
                .id(7L)
                .username("u2")
                .passwordHash("old")
                .tenantId(1L)
                .role(AppUserRole.USER)
                .active(true)
                .build();
        when(repository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-pass")).thenReturn("new-hash");
        when(repository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setPassword("new-pass");

        var updated = service.updatePassword(7L, dto);
        assertEquals(7L, updated.getId());
    }
}
