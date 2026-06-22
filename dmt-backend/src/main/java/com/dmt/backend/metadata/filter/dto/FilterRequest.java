package com.dmt.backend.metadata.filter.dto;

import com.dmt.backend.metadata.filter.entity.FilterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FilterRequest(

        @NotNull(message = "Screen id is required")
        Long screenId,

        @NotBlank(message = "Filter name is required")
        @Size(max = 150, message = "Filter name must be at most 150 characters")
        String filterName,

        @NotBlank(message = "Column name is required")
        String columnName,

        @NotNull(message = "Filter type is required")
        FilterType filterType,

        Boolean required,

        String defaultValue,

        Integer displayOrder,

        String dropdownQuery

) {
}