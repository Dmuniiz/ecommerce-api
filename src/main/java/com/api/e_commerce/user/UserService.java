package com.api.e_commerce.user;

import com.api.e_commerce.user.dto.UserUpdateRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User update(User user, UserUpdateRequest data);

    User create(String name, String email, String password);

}
