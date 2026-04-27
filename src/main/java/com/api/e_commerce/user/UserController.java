package com.api.e_commerce.user;

import com.api.e_commerce.user.dto.UserResponse;
import com.api.e_commerce.user.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User userPrincipal) {
        return Optional.ofNullable(UserResponse.fromEntity(userPrincipal))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ValidationException("User mapping failed or user not found"));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> putUserProfile(@AuthenticationPrincipal User user, @RequestBody @Valid UserUpdateRequest data) {

        var updatedUser = userService.update(user, data);
        var response = UserResponse.fromEntity(updatedUser);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(response);
    }



}
