package com.dmt.backend.auth.dto;

public record RegisterRequest(
        String username,
        String password
) {
}
