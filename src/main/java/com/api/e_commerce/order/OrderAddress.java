package com.api.e_commerce.order;

import com.api.e_commerce.address.Address;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class OrderAddress {
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    public OrderAddress(Address address) {
        this.street = address.getStreet();
        this.number = address.getNumber();
        this.complement = address.getComplement();
        this.neighborhood = address.getNeighborhood();
        this.city = address.getCity();
        this.state = address.getState();
        this.zipCode = address.getZipCode();
        this.country = address.getCountry();
    }
}
