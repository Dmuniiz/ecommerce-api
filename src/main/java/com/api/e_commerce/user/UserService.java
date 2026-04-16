package com.api.e_commerce.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User updateUser();

    User registerUser(String name, String email, String password);

}
