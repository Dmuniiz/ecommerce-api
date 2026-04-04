package com.api.e_commerce.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<UserDetails> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

}
