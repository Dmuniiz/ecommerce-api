package com.api.e_commerce.user;

import com.api.e_commerce.user.dto.UserUpdateRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    User update(User user, UserUpdateRequest data);

    User create(String name, String email, String password);

   User findUserById(UUID id);
}
