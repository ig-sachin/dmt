package com.dmt.backend.metadata.dropdown.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DropdownRequest(

        @NotBlank(message = "Dropdown code is required")
        @Size(max = 100, message = "Dropdown code must be at most 100 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "Dropdown code may only contain letters, numbers, underscores, and hyphens")
        String dropdownCode,

        @NotBlank(message = "Dropdown name is required")
        @Size(max = 150, message = "Dropdown name must be at most 150 characters")
        String dropdownName,

        @NotBlank(message = "Query is required")
        String query,

        Boolean active

) {
}