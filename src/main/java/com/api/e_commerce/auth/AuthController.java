package com.api.e_commerce.auth;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {

        var accessToken = authService.login(loginRequest.email(), loginRequest.password());

        return ResponseEntity.ok(new AuthResponse(accessToken));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterUserRequest data, UriComponentsBuilder componentsBuilder) {

        var user = authService.register(data);
        var uri = componentsBuilder.path("/api/v1/auth/{id}").buildAndExpand(user.getUsername()).toUri();

        return ResponseEntity.created(uri).body(new AuthResponse(authService.login(user.getUsername(), data.password())));
    }


}
