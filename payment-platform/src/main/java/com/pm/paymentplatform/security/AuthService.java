package com.pm.paymentplatform.security;

import com.pm.paymentplatform.merchant.Merchant;
import com.pm.paymentplatform.merchant.MerchantRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final MerchantRepository merchantRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MerchantRepository merchantRepository,
                       JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.merchantRepository = merchantRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Merchant merchant = merchantRepository.findMerchantByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), merchant.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(merchant);
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);

        return response;
    }
}
