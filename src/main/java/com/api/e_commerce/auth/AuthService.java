package com.api.e_commerce.auth;


import com.api.e_commerce.auth.dto.RegisterUserRequest;

public interface AuthService {

    String getLoginToken(String email, String password);
    String register(RegisterUserRequest data);
}
