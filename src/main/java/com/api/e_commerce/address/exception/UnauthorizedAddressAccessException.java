package com.api.e_commerce.address.exception;

import java.util.UUID;

public class UnauthorizedAddressAccessException extends RuntimeException {

    public UnauthorizedAddressAccessException(UUID addressId) {
        super("Unauthorized access to address: " + addressId);
    }

    public UnauthorizedAddressAccessException(String message) {
        super(message);
    }
}

