package com.manifactory.backend.publicapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.Data;

@Data
public class PublicOrderRequestDTO {

    @NotBlank
    private String name;

    private String businessName;
    private String phone;
    private String city;
    private String preferredTransport;

    @NotEmpty
    @Valid
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull
        private Long productId;

        @NotNull
        @Positive
        private Integer quantity;
    }
}
