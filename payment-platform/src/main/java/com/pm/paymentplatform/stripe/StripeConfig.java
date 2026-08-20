package com.pm.paymentplatform.stripe;

import com.stripe.StripeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Bean
    public StripeClient stripeClient(@Value("${stripe.api-key}") String apiKey) {
        return new StripeClient(apiKey);
    }
}
