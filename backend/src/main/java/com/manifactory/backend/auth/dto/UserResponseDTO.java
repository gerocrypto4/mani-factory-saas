package com.manifactory.backend.auth.dto;

import com.manifactory.backend.auth.entity.AppUserRole;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private Long tenantId;
    private AppUserRole role;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
