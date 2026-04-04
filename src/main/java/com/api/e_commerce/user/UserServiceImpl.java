package com.api.e_commerce.user;

import com.api.e_commerce.config.exception.ValidationBusinessException;
import com.api.e_commerce.config.security.TokenService;
import com.api.e_commerce.role.Role;
import com.api.e_commerce.role.RoleRepository;
import com.api.e_commerce.role.RoleType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final TokenService tokenService;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Override
    @Transactional
    public User registerUser(String name, String email, String password) {
        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("Email already in use: " + email);
        }

        Role userRole = roleRepository.findByRoleName(RoleType.USER)
                .orElseThrow(() -> new ValidationBusinessException("Default user role not found"));

        User newUser = new User(
                null,
                name,
                email,
                password,
                LocalDateTime.now(),
                List.of(userRole)
        );

        return userRepository.save(newUser);
    }

    @Override
    public UserDetails validateToken(String token) {
        String subject = tokenService.extractSubject(token);
       return loadUserByUsername(subject);
    }

}
