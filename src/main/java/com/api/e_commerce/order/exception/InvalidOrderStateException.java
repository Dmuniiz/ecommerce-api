package com.api.e_commerce.order.exception;

import com.api.e_commerce.order.OrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }

    public InvalidOrderStateException(OrderStatus currentStatus, String operation) {
        super("Cannot " + operation + " order in state: " + currentStatus);
    }
}

