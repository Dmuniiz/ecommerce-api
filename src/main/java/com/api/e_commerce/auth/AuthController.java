package com.api.e_commerce.auth;


import com.api.e_commerce.auth.dto.*;
import com.api.e_commerce.auth.dto.TokenRefreshRequest;
import com.api.e_commerce.user.User;
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
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Refresh-Token") @Valid TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.refreshToken()));
    }


    @PostMapping("/signup")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterUserRequest data, UriComponentsBuilder componentsBuilder) {
        var newUser = authService.register(data);
        //
        var uri = componentsBuilder
                .path("/api/v1/auth/me")
                .buildAndExpand(newUser.getId()).toUri();

        //return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(accessToken));
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                new UserResponse(user.getId(), user.getName(), user.getEmail())
        );
    }

}
