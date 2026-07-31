package com.pm.paymentplatform.merchant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/merchants/register")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;

    }

    @PostMapping
    public ResponseEntity<MerchantRegistrationResponseDTO> registerMerchant(@Valid @RequestBody MerchantRegistrationRequestDTO request) {
        MerchantRegistrationResponseDTO response = merchantService.registerMerchant(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
