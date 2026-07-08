package com.api.e_commerce.address.dto;

import com.api.e_commerce.address.AddressType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        Set<AddressType> types,
        Boolean isDefault,
        Instant createdAt,
        Instant updatedAt
) { }

