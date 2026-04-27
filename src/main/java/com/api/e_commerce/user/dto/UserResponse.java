package com.api.e_commerce.user.dto;

import com.api.e_commerce.role.Role;
import com.api.e_commerce.user.User;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, List<Role> roles, String cpf, LocalDate birthDate, String phoneNumber, Instant createdAt, Instant updateAt ) {

    public static UserResponse fromEntity(User user){
        return new UserResponse(user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles(),
                user.getCpf(),
                user.getBirthDate(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
