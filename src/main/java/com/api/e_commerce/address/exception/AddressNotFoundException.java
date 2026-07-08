package com.api.e_commerce.address.exception;

import java.util.UUID;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(UUID addressId) {
        super("Address not found with ID: " + addressId);
    }

    public AddressNotFoundException(String message) {
        super(message);
    }
}

