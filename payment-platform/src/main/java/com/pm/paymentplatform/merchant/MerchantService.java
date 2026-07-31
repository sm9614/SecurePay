package com.pm.paymentplatform.merchant;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MerchantService {

    private final PasswordEncoder passwordEncoder;
    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository, PasswordEncoder passwordEncoder) {
        this.merchantRepository = merchantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public MerchantRegistrationResponseDTO registerMerchant(MerchantRegistrationRequestDTO request) {

        if (merchantRepository.findMerchantByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyRegisteredException("The email: " + request.getEmail() + " has already been registered");
        }

        Merchant merchant = new Merchant();
        merchant.setEmail(request.getEmail());
        merchant.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        try {
            merchantRepository.saveAndFlush(merchant);

        }catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyRegisteredException("The email: " + request.getEmail() + " has already been registered");
        }

        return MerchantMapper.toResponseDTO(merchant);
    }
}
