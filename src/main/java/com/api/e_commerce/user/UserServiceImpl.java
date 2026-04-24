package com.api.e_commerce.user;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.config.security.TokenService;
import com.api.e_commerce.role.Role;
import com.api.e_commerce.role.RoleRepository;
import com.api.e_commerce.role.RoleType;
import com.api.e_commerce.user.dto.UserUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    /*@Override
    public User updateUser(User user, UserUpdateRequest data) {



    }*/

    @Override
    @Transactional
    public User registerUser(String name, String email, String password) {
        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("Email already in use: " + email);
        }

        List<Role> defaultRole  = roleRepository.findByRoleName(RoleType.USER)
                .map(List::of)
                .orElseGet(ArrayList::new);

        System.out.println("defaultRole = " + defaultRole.getFirst().getAuthority());

        User newUser = new User(
                name,
                email,
                password,
                defaultRole
        );


        return userRepository.save(newUser);
    }

}
