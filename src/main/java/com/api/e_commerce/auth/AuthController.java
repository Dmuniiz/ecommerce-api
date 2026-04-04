package com.api.e_commerce.auth;


import com.api.e_commerce.auth.dto.AuthResponse;
import com.api.e_commerce.auth.dto.LoginRequest;
import com.api.e_commerce.auth.dto.RegisterUserRequest;
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
        String tokenJWT = authService.getLoginToken(loginRequest.email(), loginRequest.password());
        return ResponseEntity.ok(new AuthResponse (tokenJWT));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterUserRequest data, UriComponentsBuilder componentsBuilder) {

        authService.register(data);
        var uri = componentsBuilder.path("/api/v1/auth/{id}").buildAndExpand(data.email()).toUri();

        //return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(accessToken));
        return ResponseEntity.created(uri).build();
    }




}
