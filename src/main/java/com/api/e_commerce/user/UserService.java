package com.api.e_commerce.user;

import com.api.e_commerce.user.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    User create(String name, String email, String password);

    User update(User user, UserUpdateRequest data);


    User findUserById(UUID id);;

    boolean existsByEmail(String email);

    void deleteUser(UUID userId);

    Page<User> getAllUsers(Pageable pageable);

}
