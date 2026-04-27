package com.api.e_commerce.address.dto;

import com.api.e_commerce.address.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateAddressRequest(

        @NotBlank(message = "The street is required")
        @Size(max = 255)
        String street,

        @NotBlank(message = "Address number is required")
        @Size(max = 20)
        String number,

        @Size(max = 100)
        String complement,

        @NotBlank(message = "Neighborhood is required")
        @Size(max = 100)
        String neighborhood,

        @NotBlank(message = "City is required")
        @Size(max = 100)
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 100)
        String state,

        @NotBlank(message = "The postal code is required")
        @Size(max = 20)
        String zipCode,

        @NotBlank(message = "Country is required")
        @Size(max = 100)
        String country,

        @NotNull
        Boolean isDefault,

        @NotEmpty(message = "Enter at least one type of address")
        Set<AddressType> addressType

) {
}
