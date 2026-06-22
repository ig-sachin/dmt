package com.dmt.backend.metadata.screen.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ScreenRequest(

        @NotBlank(message = "Screen code is required")
        @Size(max = 50, message = "Screen code must be at most 50 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]+$",
                message = "Screen code may only contain letters, numbers, underscores, and hyphens")
        String screenCode,

        @NotBlank(message = "Screen name is required")
        @Size(max = 150, message = "Screen name must be at most 150 characters")
        String screenName,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotBlank(message = "Select query is required")
        String selectQuery,

        @Min(value = 1, message = "Default page size must be at least 1")
        @Max(value = 500, message = "Default page size must be at most 500")
        Integer defaultPageSize,

        String defaultSortColumn,

        @Pattern(
                regexp = "^(?i)(ASC|DESC)$",
                message = "Default sort direction must be ASC or DESC")
        String defaultSortDirection,

        Boolean active,

        @NotBlank(message = "Primary key column is required")
        String primaryKeyColumn
) {
}