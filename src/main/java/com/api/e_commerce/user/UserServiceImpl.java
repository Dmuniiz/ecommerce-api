package com.api.e_commerce.user;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.role.RoleService;
import com.api.e_commerce.role.RoleType;
import com.api.e_commerce.user.dto.UserUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final IUserRepository userRepository;
    private final RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Override
    @Transactional
    public User update(User user, UserUpdateRequest data) {
        if (data.email() != null && !data.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(data.email())) {
                throw new ValidationException("Email is already being used");
            }
            user.setEmail(data.email());
        }

        if (data.name() != null) user.setName(data.name());
        if (data.cpf() != null) user.setCpf(data.cpf());
        if (data.phoneNumber() != null) user.setPhoneNumber(data.phoneNumber());
        if (data.birthDate() != null) user.setBirthDate(data.birthDate());

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User create(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }

        var role = roleService.addRole(RoleType.USER);

        var authUser = new User(
                name,
                email,
                password,
                role
        );
        return userRepository.save(authUser);
    }

    @Override
    public User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("User not found"));
    }


}
