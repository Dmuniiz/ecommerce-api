package com.api.e_commerce.payment.dto;

public record AbacatePayProductItem(

        String id,                  // ID do produto cadastrado na AbacatePay ou External ID
        Integer quantity) {
}
