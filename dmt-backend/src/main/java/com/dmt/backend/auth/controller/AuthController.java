package com.dmt.backend.auth.controller;

import com.dmt.backend.auth.dto.AuthResponse;
import com.dmt.backend.auth.dto.LoginRequest;
import com.dmt.backend.auth.dto.RegisterRequest;
import com.dmt.backend.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(
            @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request) {

        return authService.login(request);
    }
}