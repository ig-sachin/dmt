package com.dmt.backend.common.config;

import com.dmt.backend.role.entity.Role;
import com.dmt.backend.role.repository.RoleRepository;
import com.dmt.backend.user.entity.User;
import com.dmt.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Creates the base roles on every startup, and optionally a bootstrap admin user.
 * The bootstrap admin is entirely driven by app.bootstrap-admin.* configuration -
 * there is no hardcoded fallback username/password. If it is not explicitly enabled
 * with both a username and password supplied, no bootstrap admin is created at all,
 * which fails safe rather than silently creating a known account in any environment
 * where the config block was forgotten.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.enabled:false}")
    private boolean bootstrapAdminEnabled;

    @Value("${app.bootstrap-admin.username:}")
    private String bootstrapAdminUsername;

    @Value("${app.bootstrap-admin.password:}")
    private String bootstrapAdminPassword;

    @Override
    public void run(String... args) {

        log.info("Starting default data initialization");

        Role adminRole =
                createRoleIfNotExists("ROLE_ADMIN");

        createRoleIfNotExists("ROLE_USER");
        createRoleIfNotExists("ROLE_VIEWER");

        createBootstrapAdminIfConfigured(adminRole);
    }

    private void createBootstrapAdminIfConfigured(Role adminRole) {

        if (!bootstrapAdminEnabled) {
            log.info("Bootstrap admin creation skipped reason=disabled");
            return;
        }

        if (bootstrapAdminUsername == null || bootstrapAdminUsername.isBlank()
                || bootstrapAdminPassword == null || bootstrapAdminPassword.isBlank()) {
            log.warn(
                    "Bootstrap admin creation skipped reason=username_or_password_not_configured. " +
                            "Set app.bootstrap-admin.username and app.bootstrap-admin.password to enable it."
            );
            return;
        }

        if (userRepository.findByUsername(bootstrapAdminUsername).isPresent()) {
            log.info("Bootstrap admin already exists username={}", bootstrapAdminUsername);
            return;
        }

        User admin = new User();

        admin.setUsername(bootstrapAdminUsername);
        admin.setPassword(
                passwordEncoder.encode(bootstrapAdminPassword));

        admin.setRoles(
                Set.of(adminRole));

        userRepository.save(admin);

        log.info(
                "Bootstrap admin user created username={}. " +
                        "Change this password or disable app.bootstrap-admin.enabled after first login.",
                admin.getUsername()
        );
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