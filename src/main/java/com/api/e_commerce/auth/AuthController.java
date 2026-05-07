package com.api.e_commerce.auth;


import com.api.e_commerce.auth.dto.*;
import com.api.e_commerce.auth.dto.RefreshTokenRequest;
import com.api.e_commerce.config.security.services.RefreshTokenService;
import com.api.e_commerce.config.security.services.TokenProvider;
import com.api.e_commerce.user.User;
import com.api.e_commerce.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager manager;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
        var user = authService.login(manager.authenticate(authenticationToken));

        String accessToken =  tokenProvider.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Refresh-Token") @Valid RefreshTokenRequest request) {
        String oldToken = request.refreshToken();

        refreshTokenService.verifyExpiration(oldToken);

        var user = authService.findUserById(
                refreshTokenService.getUserIdByRefreshToken(oldToken)
        );

        var refreshToken = refreshTokenService.createRefreshToken(user.getId());
        String accessToken = tokenProvider.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getToken()));
    }


    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterUserRequest data, UriComponentsBuilder componentsBuilder) {
        var newUser = authService.register(data);
        //
        var uri = componentsBuilder
                .path("/api/v1/auth/me")
                .buildAndExpand(newUser.getId())
                .encode()
                .toUri();

        //return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(accessToken));
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

}
