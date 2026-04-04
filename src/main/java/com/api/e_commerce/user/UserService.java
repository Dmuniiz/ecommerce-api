package com.api.e_commerce.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    UserDetails validateToken(String token);
    User registerUser(String name, String email, String password);

}
