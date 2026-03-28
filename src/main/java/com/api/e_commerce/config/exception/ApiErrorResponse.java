package com.api.e_commerce.config.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String traceId;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private final LocalDateTime timestamp;

    @Builder.Default
    private final List<FieldError> errors = new ArrayList<>();

    // método controlado (opcional)
    public void addError(String field, String message) {
        this.errors.add(new FieldError(field, message));
    }
}