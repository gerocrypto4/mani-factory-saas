package com.manifactory.backend.publicapi.controller;

import com.manifactory.backend.publicapi.dto.PublicOrderRequestDTO;
import com.manifactory.backend.publicapi.dto.PublicOrderResponseDTO;
import com.manifactory.backend.publicapi.service.PublicOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/orders")
public class PublicOrderController {

    private final PublicOrderService publicOrderService;

    public PublicOrderController(PublicOrderService publicOrderService) {
        this.publicOrderService = publicOrderService;
    }

    @PostMapping
    public ResponseEntity<PublicOrderResponseDTO> createOrder(@RequestParam Long tenantId,
            @Valid @RequestBody PublicOrderRequestDTO request) {
        PublicOrderResponseDTO response = publicOrderService.createOrder(tenantId, request);
        return ResponseEntity.status(201).body(response);
    }
}
