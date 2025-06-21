package com.ecommerce.paymentservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class StripeApiProperties {
    @Value("${stripe.api.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.api.public-key}")
    private String stripeSigningKey;

    @Value("${stripe.api.webhook.sg-key}")
    private String webhookSigningKey;

    @Value("${stripe.checkout.session.duration.minutes:30}")
    private Long durationInMinutes;

}
