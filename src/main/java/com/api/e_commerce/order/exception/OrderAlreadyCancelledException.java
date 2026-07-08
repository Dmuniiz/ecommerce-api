package com.api.e_commerce.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OrderAlreadyCancelledException extends RuntimeException {
    public OrderAlreadyCancelledException(UUID orderId) {
        super("Order " + orderId + " has already been cancelled");
    }

    public OrderAlreadyCancelledException(String message) {
        super(message);
    }
}

