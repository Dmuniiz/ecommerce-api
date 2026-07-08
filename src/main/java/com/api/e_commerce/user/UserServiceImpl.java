package com.api.e_commerce.user;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.role.RoleService;
import com.api.e_commerce.role.RoleType;
import com.api.e_commerce.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final IUserRepository userRepository;
    private final RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        return userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });
    }

    @Override
    @Transactional
    public User create(String name, String email, String password) {
        log.info("Creating new user with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }

        var role = roleService.addRole(RoleType.USER);
        var authUser = new User(name, email, password, role);

        User savedUser = userRepository.save(authUser);
        log.info("User created successfully: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public User update(User user, UserUpdateRequest data) {
        log.info("Updating user: {}", user.getId());

        if (data.email() != null && !data.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(data.email())) {
                log.warn("Email already in use: {}", data.email());
                throw new ValidationException("Email is already being used");
            }
            user.setEmail(data.email());
        }

        if (data.name() != null) user.setName(data.name());
        if (data.cpf() != null) user.setCpf(data.cpf());
        if (data.phoneNumber() != null) user.setPhoneNumber(data.phoneNumber());
        if (data.birthDate() != null) user.setBirthDate(data.birthDate());

        User updatedUser = userRepository.save(user);
        log.info("User {} updated successfully", user.getId());

        return updatedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserById(UUID id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new ValidationException("User not found");
                });
    }

    //=============ADMIN METHODS==========

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user: {}", userId);

        User user = findUserById(userId);
        userRepository.delete(user);

        log.info("User {} deleted successfully", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users with pagination");
        return userRepository.findAll(pageable);
    }
}
