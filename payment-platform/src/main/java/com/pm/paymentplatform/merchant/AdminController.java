package com.pm.paymentplatform.merchant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/merchants")
public class AdminController {

    private final MerchantRepository merchantRepository;

    public AdminController (MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @GetMapping
    public ResponseEntity<List<MerchantRegistrationResponseDTO>> listAllMerchants() {
        List<Merchant> merchants = merchantRepository.findAll();
        List<MerchantRegistrationResponseDTO> response =
                merchants
                .stream()
                .map(MerchantMapper::toResponseDTO)
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
