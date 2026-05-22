package com.manifactory.backend.clients.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateClientDTO {
    @NotBlank
    private String name;

    private String businessName;

    private String email;

    private String phone;

    private String city;

    private String preferredTransport;
}
