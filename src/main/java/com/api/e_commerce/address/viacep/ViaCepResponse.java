package com.api.e_commerce.address.viacep;

public record ViaCepResponse(
        String zipCode,
        String street,
        String state,
        String neighborhood,
        String city,
        Boolean error
) { }
