package com.manifactory.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePasswordDTO {
    @NotBlank
    private String password;
}
