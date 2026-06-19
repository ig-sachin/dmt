package com.dmt.backend.auth.dto;

public record LoginRequest(
        String username,
        String password
) {}
