package com.api.e_commerce.auth;

import com.api.e_commerce.auth.dto.RegisterUserRequest;
import com.api.e_commerce.user.User;
import com.api.e_commerce.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    public User login(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }


    public User register(RegisterUserRequest data) {
        String passwordEncoded = passwordEncoder.encode(data.password());
        return userService.create(data.name(), data.email(), passwordEncoded);
    }

    public User findUserById(UUID id) {
       return userService.findUserById(id);
    }
}


