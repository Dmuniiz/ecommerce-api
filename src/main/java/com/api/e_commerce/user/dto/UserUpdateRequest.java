package com.api.e_commerce.user.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record UserUpdateRequest(
        @NotBlank String name,
        @Email String email,

        @NotBlank
        @Size(min = 10, max = 15, message = "Phone number length must be between 10 and 15")
        String phoneNumber,

        @CPF // real and valid cpf
        String cpf,

        @Past
        LocalDate birthDate)
{ }
