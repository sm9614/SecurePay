package com.pm.paymentplatform.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/merchants/me/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<ApiKeyResponseDTO> createApiKey() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID merchantId = (UUID) authentication.getPrincipal();
        ApiKeyResponseDTO response = apiKeyService.createApiKey(merchantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
