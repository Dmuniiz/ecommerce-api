package com.api.e_commerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelOrderRequest(
        @NotBlank(message = "Reason is required")
        @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
        String reason
) {
}

