package com.api.e_commerce.auth.dto;

import jakarta.validation.constraints.*;

public record RegisterUserRequest(

        @NotBlank
        String name,

        @NotBlank
        @Email(message = "Email should be valid")
        String email,

        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password

        )
{ }
