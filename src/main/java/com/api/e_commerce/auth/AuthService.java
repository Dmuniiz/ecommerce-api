package com.api.e_commerce.auth;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {

    String login(String email, String password);
    UserDetails register(RegisterUserRequest data);

}
