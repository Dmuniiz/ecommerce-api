package com.api.e_commerce.user.dto;

import com.api.e_commerce.role.Role;
import com.api.e_commerce.role.RoleType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, List<Role> roles, Instant createdAt ) {
}
