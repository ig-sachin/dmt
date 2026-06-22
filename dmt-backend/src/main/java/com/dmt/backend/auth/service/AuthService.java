package com.dmt.backend.auth.service;

import com.dmt.backend.auth.dto.AuthResponse;
import com.dmt.backend.auth.dto.LoginRequest;
import com.dmt.backend.auth.dto.RegisterRequest;
import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.role.entity.Role;
import com.dmt.backend.role.repository.RoleRepository;
import com.dmt.backend.security.JwtService;
import com.dmt.backend.user.entity.User;
import com.dmt.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        }

        List<String> requestedRoleNames =
                (request.roleNames() == null || request.roleNames().isEmpty())
                        ? List.of("ROLE_USER")
                        : request.roleNames();

        Set<Role> roles = requestedRoleNames.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> {
                            log.warn("Register failed username={} reason=unknown_role roleName={}", request.username(), roleName);
                            return new ApiException(HttpStatus.BAD_REQUEST, "Unknown role: " + roleName);
                        }))
                .collect(Collectors.toSet());

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .roles(roles)
                .enabled(true)
                .build();

        userRepository.save(user);

        log.info("User registered successfully username={} roles={}", request.username(), requestedRoleNames);

        return "User registered successfully";
    }

    public AuthResponse login(LoginRequest request) {

        log.info("Login request received username={}", request.username());

        User user = userRepository.findByUsername(request.username())
                .orElse(null);

        // Same outward message whether the username doesn't exist or the password is
        // wrong, so a caller can't use this endpoint to enumerate valid usernames.
        // The specific reason is still logged server-side for diagnostics.
        if (user == null) {
            log.warn("Login failed username={} reason=user_not_found", request.username());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            log.warn("Login failed username={} reason=account_disabled", request.username());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword())) {

            log.warn("Login failed username={} reason=invalid_credentials", request.username());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());

        log.info("Login successful username={}", request.username());

        return new AuthResponse(token);
    }
}