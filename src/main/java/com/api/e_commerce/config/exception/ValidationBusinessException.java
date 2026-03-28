package com.api.e_commerce.config.exception;



import java.util.List;

public class ValidationBusinessException extends RuntimeException {

    private final List<FieldError> errors;

    public ValidationBusinessException(String message, List<FieldError> errors) {
        super(message);
        this.errors = errors;
    }

    public List<FieldError> getErrors() {
        return errors;
    }
}