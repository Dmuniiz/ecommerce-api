package com.api.e_commerce.auth;


import com.api.e_commerce.auth.dto.*;
import com.api.e_commerce.auth.dto.RefreshTokenRequest;
import com.api.e_commerce.user.User;
import com.api.e_commerce.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        var response = authService.login(loginRequest.email(), loginRequest.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Refresh-Token") @Valid RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.refreshToken()));
    }


    @PostMapping("/register")
    public ResponseEntity<Void> createAuthUser(@RequestBody @Valid RegisterUserRequest data, UriComponentsBuilder componentsBuilder) {
        var newUser = authService.createAuthUser(data);
        //
        var uri = componentsBuilder
                .path("/api/v1/auth/me")
                .buildAndExpand(newUser.getId()).toUri();

        //return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(accessToken));
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

}
