package com.manifactory.backend.auth.dto;

import com.manifactory.backend.auth.entity.AppUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotNull
    private Long tenantId;

    @NotNull
    private AppUserRole role;

    private Boolean active;
}
