package com.dmt.backend.common.config;

import com.dmt.backend.role.entity.Role;
import com.dmt.backend.role.repository.RoleRepository;
import com.dmt.backend.user.entity.User;
import com.dmt.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        log.info("Starting default data initialization");

        Role adminRole =
                createRoleIfNotExists("ROLE_ADMIN");

        createRoleIfNotExists("ROLE_USER");
        createRoleIfNotExists("ROLE_VIEWER");

        if (userRepository.findByUsername("sachin").isEmpty()) {

            User admin = new User();

            admin.setUsername("sachin");
            admin.setPassword(
                    passwordEncoder.encode("sachin123"));

            admin.setRoles(
                    Set.of(adminRole));

            userRepository.save(admin);

            log.info("Default admin user created username={}", admin.getUsername());
        } else {
            log.info("Default admin user already exists username={}", "sachin");
        }
    }

    private Role createRoleIfNotExists(
            String roleName) {

        return roleRepository
                .findByRoleName(roleName)
                .orElseGet(() -> {

                    log.info("Creating role roleName={}", roleName);

                    Role role = new Role();

                    role.setRoleName(roleName);

                    return roleRepository.save(role);
                });
    }
}
