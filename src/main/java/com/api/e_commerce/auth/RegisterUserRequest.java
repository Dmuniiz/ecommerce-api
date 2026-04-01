package com.api.e_commerce.auth;

import com.api.e_commerce.role.RoleType;
import jakarta.validation.constraints.*;

public record RegisterUserRequest(

        @NotBlank
        String name,

        @NotBlank
        @Email(message = "Email should be valid")
        String email,

        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotNull(message = "Role name cannot be null")
        RoleType role

        )
{ }
