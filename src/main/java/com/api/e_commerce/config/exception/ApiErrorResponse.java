package com.api.e_commerce.config.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(

        int status,
        String error,
        String message,
        String path,
        String traceId,

        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime timestamp,

        List<FieldError> errors
) {
}