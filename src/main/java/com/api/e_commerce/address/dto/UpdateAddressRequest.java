package com.api.e_commerce.address.dto;

import com.api.e_commerce.address.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateAddressRequest(
        @NotBlank String number,
        @NotBlank String complement,
        @NotEmpty Set<AddressType> addressType
) {}
