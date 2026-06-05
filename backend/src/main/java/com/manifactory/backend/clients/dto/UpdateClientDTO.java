package com.manifactory.backend.clients.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateClientDTO {
    @NotBlank(message = "Client name is required")
    @Size(max = 255, message = "Client name must be at most 255 characters")
    private String name;

    @Size(max = 255, message = "Business name must be at most 255 characters")
    private String businessName;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @Size(max = 255, message = "Phone must be at most 255 characters")
    private String phone;

    @Size(max = 255, message = "City must be at most 255 characters")
    private String city;

    @Size(max = 255, message = "Preferred transport must be at most 255 characters")
    private String preferredTransport;
}
