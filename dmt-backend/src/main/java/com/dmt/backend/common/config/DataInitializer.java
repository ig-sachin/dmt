package com.dmt.backend.common.config;

import com.dmt.backend.role.entity.Role;
import com.dmt.backend.role.repository.RoleRepository;
import com.dmt.backend.user.entity.User;
import com.dmt.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        Role adminRole =
                createRoleIfNotExists("ROLE_ADMIN");

        createRoleIfNotExists("ROLE_USER");
        createRoleIfNotExists("ROLE_VIEWER");

        if (userRepository.findByUsername("admin").isEmpty()) {

            User admin = new User();

            admin.setUsername("sachin");
            admin.setPassword(
                    passwordEncoder.encode("sachin123"));

            admin.setRoles(
                    Set.of(adminRole));

            userRepository.save(admin);

            System.out.println(
                    "Default admin user created");
        }
    }

    private Role createRoleIfNotExists(
            String roleName) {

        return roleRepository
                .findByRoleName(roleName)
                .orElseGet(() -> {

                    Role role = new Role();

                    role.setRoleName(roleName);

                    return roleRepository.save(role);
                });
    }
}