package com.api.e_commerce.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedOrderAccessException extends RuntimeException {
    public UnauthorizedOrderAccessException(UUID orderId, UUID userId) {
        super("User " + userId + " is not authorized to access order " + orderId);
    }

    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
}

