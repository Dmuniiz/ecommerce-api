package com.api.e_commerce.auth;


import com.api.e_commerce.auth.dto.RegisterUserRequest;
import com.api.e_commerce.config.security.TokenService;
import com.api.e_commerce.user.User;
import com.api.e_commerce.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final AuthenticationManager manager;

    private final UserService userService;
    private final TokenService tokenService;

    private final PasswordEncoder passwordEncoder;


    @Override
    public String getLoginToken(String email, String password) {

        var user = userService.loadUserByUsername(email);
        var authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), password);

        var authenticationManager = manager.authenticate(authenticationToken);

        var userDetails = (User) authenticationManager.getPrincipal();

        return tokenService.generateToken(userDetails);
    }

    @Override
    public String register(RegisterUserRequest data) {
        String passwordEncoded = passwordEncoder.encode(data.password());
        var user = userService.registerUser(data.name(), data.email(), passwordEncoded);
        return tokenService.generateToken(user);
    }


}


