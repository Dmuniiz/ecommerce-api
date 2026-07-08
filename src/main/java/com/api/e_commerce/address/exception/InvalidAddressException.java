package com.api.e_commerce.address.exception;

public class InvalidAddressException extends RuntimeException {

    public InvalidAddressException(String message) {
        super(message);
    }

    public InvalidAddressException(String field, String sent, String expected) {
        super(String.format("Field '%s' divergence: sent=%s, expected=%s", field, sent, expected));
    }
}

