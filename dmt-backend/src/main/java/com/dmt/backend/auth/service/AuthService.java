package com.dmt.backend.auth.service;

import com.dmt.backend.auth.dto.AuthResponse;
import com.dmt.backend.auth.dto.LoginRequest;
import com.dmt.backend.auth.dto.RegisterRequest;
import com.dmt.backend.role.entity.Role;
import com.dmt.backend.role.repository.RoleRepository;
import com.dmt.backend.security.JwtService;
import com.dmt.backend.user.entity.User;
import com.dmt.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest request) {

        log.info("Register request received username={}", request.username());

        if (userRepository.findByUsername(request.username()).isPresent()) {
            log.warn("Register failed username={} reason=username_exists", request.username());
            throw new RuntimeException("Username already exists");
        }

        Role role = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> {
                    log.error("Register failed username={} reason=default_role_not_found", request.username());
                    return new RuntimeException("Default role not found");
                });

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(role))
                .build();

        userRepository.save(user);

        log.info("User registered successfully username={}", request.username());

        return "User registered successfully";
    }

    public AuthResponse login(LoginRequest request) {

        log.info("Login request received username={}", request.username());

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    log.warn("Login failed username={} reason=user_not_found", request.username());
                    return new RuntimeException("User not found");
                });

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword())) {

            log.warn("Login failed username={} reason=invalid_credentials", request.username());
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getUsername());

        log.info("Login successful username={}", request.username());

        return new AuthResponse(token);
    }
}
