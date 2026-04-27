package com.api.e_commerce.address.dto;

import com.api.e_commerce.address.Address;
import com.api.e_commerce.address.AddressType;
import com.api.e_commerce.user.User;

import java.util.Set;
import java.util.UUID;

public record AddressesResponse(
        UUID userId,
        String userName,
        UUID addressId,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        boolean isDefault,
        Set<AddressType> types) {


    public static AddressesResponse fromEntity(Address addr, User user) {
        return new AddressesResponse(
                user.getId(),
                user.getName(),
                addr.getId(),
                addr.getStreet(),
                addr.getNumber(),
                addr.getComplement(),
                addr.getNeighborhood(),
                addr.getCity(),
                addr.getState(),
                addr.getZipCode(),
                addr.getIsDefault(),
                addr.getAddressType()
        );
    }

}
