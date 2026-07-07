package com.api.e_commerce.auth;


import com.api.e_commerce.auth.dto.*;
import com.api.e_commerce.auth.dto.RefreshTokenRequest;
import com.api.e_commerce.config.security.services.RefreshTokenService;
import com.api.e_commerce.config.security.services.RefreshTokenService.RefreshTokenMetadata;
import com.api.e_commerce.config.security.services.TokenProvider;
import com.api.e_commerce.user.User;
import com.api.e_commerce.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletRequest request) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
        var user = authService.login(manager.authenticate(authenticationToken));

        String accessToken =  tokenProvider.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId(), refreshTokenMetadata(request)).token();

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, tokenProvider.getAccessTokenExpiresInSeconds()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest, HttpServletRequest request) {
        String oldToken = refreshTokenRequest.refreshToken();

        var refreshToken = refreshTokenService.rotateRefreshToken(oldToken, refreshTokenMetadata(request));
        var user = authService.findUserById(refreshToken.entity().getUserId());
        String accessToken = tokenProvider.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.token(), tokenProvider.getAccessTokenExpiresInSeconds()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequest request) {
        refreshTokenService.revokeRefreshToken(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal User user) {
        refreshTokenService.revokeAllUserTokens(user.getId());
        return ResponseEntity.noContent().build();
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

        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    private RefreshTokenMetadata refreshTokenMetadata(HttpServletRequest request) {
        return new RefreshTokenMetadata(
                request.getHeader("X-Device-Id"),
                clientIp(request),
                request.getHeader("User-Agent")
        );
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
