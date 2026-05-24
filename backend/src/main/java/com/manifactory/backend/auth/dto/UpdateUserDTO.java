package com.manifactory.backend.auth.dto;

import com.manifactory.backend.auth.entity.AppUserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserDTO {
    @NotNull
    private Long tenantId;

    @NotNull
    private AppUserRole role;

    @NotNull
    private Boolean active;
}
