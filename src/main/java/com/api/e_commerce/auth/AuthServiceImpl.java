package com.api.e_commerce.auth;

import com.api.e_commerce.role.RoleRepository;
import com.api.e_commerce.user.User;
import com.api.e_commerce.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final AuthenticationManager manager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String login(String email, String password) {

        //userServiceImpl.loadUserByUsername(email); -> manager
        var authenticationToken = manager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        return tokenService.generateToken((User) authenticationToken.getPrincipal());
    }

    @Override
    @Transactional
    public UserDetails register(RegisterUserRequest data) {

        var role = roleRepository.findRoleByRoleType(data.role()).orElseThrow(() -> new RuntimeException("Role not found"));
        var user = new User(data, passwordEncoder.encode(data.password()), role);

        return userRepository.save(user);
    }
}


