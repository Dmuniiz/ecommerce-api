package com.api.e_commerce.auth;

import com.api.e_commerce.user.User;
import com.api.e_commerce.user.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final AuthenticationManager manager;
    private final TokenService tokenService;


    @Override
    public String login(String email, String password) {

        //userServiceImpl.loadUserByUsername(email); -> manager

        var authenticationToken = manager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        return tokenService.generateToken((User) authenticationToken.getPrincipal());
    }
}


