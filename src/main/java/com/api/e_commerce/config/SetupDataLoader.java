package com.api.e_commerce.config;

import com.api.e_commerce.role.RoleService;
import com.api.e_commerce.role.RoleType;
import com.api.e_commerce.user.IUserRepository;
import com.api.e_commerce.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetupDataLoader implements CommandLineRunner {

    private final IUserRepository userRepository;
    private final RoleService roleService;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User adminUser = new User(
                    "ADMIN",
                    "admin@local.com",
                    "{bcrypt}$2a$12$4eOZVKqzM4iK6Q/V1kTZr.v65REjm5QkoS9LpkGpfZ7G1ld2oukJa",
                    roleService.addRole(RoleType.ADMIN)
            );
            adminUser.setCpf("00000000000");
            adminUser.setPhoneNumber("00000000000");
            adminUser.setIsActive(true);
            userRepository.save(adminUser);
        }
    }
}
