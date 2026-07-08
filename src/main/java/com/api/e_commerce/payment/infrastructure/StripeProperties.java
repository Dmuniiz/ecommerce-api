package com.api.e_commerce.payment.infrastructure;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    @Value("${stripe.secret-key}")
    @NotBlank(message = "Stripe secret key must not be blank")
    private String SECRET_KEY;

    @Value("${stripe.webhook.secret}")
    @NotBlank(message = "Webhook secret key must not be blank")
    private String WEBHOOK_SECRET;

}
