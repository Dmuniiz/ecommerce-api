package com.api.e_commerce.config.client;

import com.api.e_commerce.payment.infrastructure.AbacatePayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final AbacatePayProperties abacatePayProperties;

    @Bean
    public RestClient viaCepClient() {
        return RestClient.builder()
                .baseUrl("https://viacep.com.br/ws/")
                .build();
    }

    @Bean
    public RestClient abacatePayClient() {
        return RestClient.builder()
                .baseUrl("https://api.abacatepay.com/v2")
                .build();
    }
}

