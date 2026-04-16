package com.api.e_commerce.auth;

import com.api.e_commerce.auth.dto.AuthResponse;
import com.api.e_commerce.auth.dto.RegisterUserRequest;
import com.api.e_commerce.config.security.RefreshTokenService;
import com.api.e_commerce.config.security.TokenService;
import com.api.e_commerce.user.User;
import com.api.e_commerce.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;

    private final AuthenticationManager manager;
    private final PasswordEncoder passwordEncoder;


    public AuthResponse login(String email, String password) {

        var authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        var authenticationManager = manager.authenticate(authenticationToken);

        var user = (User) authenticationManager.getPrincipal();

        String accessToken =  tokenService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken.getToken());
    }


    public User register(RegisterUserRequest data) {
        String passwordEncoded = passwordEncoder.encode(data.password());
        return userService.registerUser(data.name(), data.email(), passwordEncoded);
    }

    public AuthResponse refreshToken(String refreshToken) {

        System.out.println("Refresh token: " + refreshToken);

       var tokenEntity = refreshTokenService.findRefreshTokenByToken(refreshToken);

        var user = tokenEntity.getUser();

       var newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
       tokenService.generateToken(user);

       return new AuthResponse(tokenService.generateToken(user),newRefreshToken.getToken());
    }

}


