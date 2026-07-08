package com.api.e_commerce.address.mapper;

import com.api.e_commerce.address.Address;
import com.api.e_commerce.address.dto.AddressResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddressMapper {

    public AddressResponse toDto(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressResponse(
            address.getId(),
            address.getStreet(),
            address.getNumber(),
            address.getComplement(),
            address.getNeighborhood(),
            address.getCity(),
            address.getState(),
            address.getZipCode(),
            address.getAddressType(),
            address.getIsDefault(),
            address.getCreatedAt(),
            address.getUpdatedAt()
        );
    }

    public List<AddressResponse> toDtoList(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return List.of();
        }

        return addresses.stream()
            .map(this::toDto)
            .toList();
    }
}

