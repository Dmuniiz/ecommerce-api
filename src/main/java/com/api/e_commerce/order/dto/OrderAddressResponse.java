package com.api.e_commerce.order.dto;

public record OrderAddressResponse(
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        String country
) {
}
