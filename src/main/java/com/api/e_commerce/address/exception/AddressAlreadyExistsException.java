package com.api.e_commerce.address.exception;

public class AddressAlreadyExistsException extends RuntimeException {

    public AddressAlreadyExistsException(String message) {
        super(message);
    }

    public AddressAlreadyExistsException(String zipCode, String number) {
        super("Address with zipCode " + zipCode + " and number " + number + " already exists for this user");
    }
}

