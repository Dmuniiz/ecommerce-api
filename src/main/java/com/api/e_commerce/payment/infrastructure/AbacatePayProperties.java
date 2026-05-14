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
@ConfigurationProperties(prefix = "abacate-pay")
public class AbacatePayProperties {

    @NotBlank(message = "Abacate secret key must not be blank")
    private String SECRET_KEY;

    @Value("${abacate-pay.webhook.secret}")
    @NotBlank(message = "Webhook secret key must not be blank")
    private String WEBHOOK_SECRET_KEY;

}
