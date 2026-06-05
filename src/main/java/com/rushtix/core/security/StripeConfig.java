package com.rushtix.core.security;

import com.stripe.Stripe;
import com.stripe.StripeClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.api.secret-key}")
    private String secretKey;

    @Bean
    public StripeClient stripeClient() {
        return new StripeClient(secretKey);
    }
}
