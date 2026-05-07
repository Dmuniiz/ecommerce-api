package com.api.e_commerce.config.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClient {

    @Bean
    public org.springframework.web.client.RestClient viaCepClient() {
        return org.springframework.web.client.RestClient.builder()
                .baseUrl("https://viacep.com.br/ws/")
                .build();
    }
}

