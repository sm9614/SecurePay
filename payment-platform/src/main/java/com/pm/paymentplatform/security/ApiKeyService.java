package com.pm.paymentplatform.security;

import com.pm.paymentplatform.merchant.Merchant;
import com.pm.paymentplatform.merchant.MerchantNotFoundException;
import com.pm.paymentplatform.merchant.MerchantRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;
    private final SecureRandom secureRandom;
    private final PasswordEncoder passwordEncoder;

    public ApiKeyService(ApiKeyRepository apiKeyRepository,
                         MerchantRepository merchantRepository,
                         SecureRandom secureRandom,
                         PasswordEncoder passwordEncoder) {
        this.apiKeyRepository = apiKeyRepository;
        this.secureRandom = secureRandom;
        this.passwordEncoder = passwordEncoder;
        this.merchantRepository = merchantRepository;
    }

    public ApiKeyResponseDTO createApiKey(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));

        byte[] prefixBytes = new byte[6];
        secureRandom.nextBytes(prefixBytes);
        String prefix = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(prefixBytes);

        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        String secret = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(secretBytes);

        ApiKey apiKey = new ApiKey();
        apiKey.setMerchant(merchant);
        apiKey.setPrefix(prefix);
        apiKey.setHashedSecret(passwordEncoder.encode(secret));

        try {
            apiKeyRepository.saveAndFlush(apiKey);
        }catch (DataIntegrityViolationException e) {
            throw new RuntimeException();
        }

        return ApiKeyMapper.toResponseDTO(apiKey, (prefix + secret));
    }
}
