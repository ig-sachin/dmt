package com.dmt.backend.auth.controller;

import com.dmt.backend.auth.dto.AuthResponse;
import com.dmt.backend.auth.dto.LoginRequest;
import com.dmt.backend.auth.dto.RegisterRequest;
import com.dmt.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Account creation is an admin operation, not public self-registration -
     * anyone who could self-register could otherwise grant themselves screen
     * access or remap procedures once logged in. @PreAuthorize here is a second,
     * independent check on top of the /api/users/** matcher in SecurityConfig.
     */
    @PostMapping("/api/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}