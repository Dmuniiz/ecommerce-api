package com.api.e_commerce.user;

import com.api.e_commerce.user.dto.UserResponse;
import com.api.e_commerce.user.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                new UserResponse(user.getId(), user.getName(), user.getEmail(),
                        user.getRoles(), user.getCreatedAt())
        );
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateCurrentUser(@AuthenticationPrincipal User user, @RequestBody @Valid UserUpdateRequest data) {
        // Implement the logic to update the user's information
        // For example, you can accept a request body with the updated information and call a service method to perform the update



        return ResponseEntity.ok(
                new UserResponse(user.getId(), user.getName(), user.getEmail(),
                        user.getRoles(), user.getCreatedAt())
        );
    }

}
