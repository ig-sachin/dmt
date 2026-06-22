package com.dmt.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3 to 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
        String password,

        /**
         * Role names to assign, e.g. ["ROLE_ADMIN"] or ["ROLE_USER", "ROLE_VIEWER"].
         * Defaults to ["ROLE_USER"] when omitted or empty.
         */
        List<String> roleNames

) {
}