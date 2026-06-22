package com.dmt.backend.metadata.column.dto;

import com.dmt.backend.metadata.column.entity.FieldType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ColumnRequest(

        @NotNull(message = "Screen id is required")
        Long screenId,

        @NotBlank(message = "Column name is required")
        @Size(max = 100, message = "Column name must be at most 100 characters")
        String columnName,

        @NotBlank(message = "Display name is required")
        @Size(max = 150, message = "Display name must be at most 150 characters")
        String displayName,

        @NotBlank(message = "Data type is required")
        String dataType,

        @NotNull(message = "Field type is required")
        FieldType fieldType,

        Boolean visible,

        Boolean editable,

        Boolean mandatory,

        String defaultValue,

        @NotNull(message = "Display order is required")
        @Min(value = 0, message = "Display order must be zero or greater")
        Integer displayOrder,

        Integer width,

        String alignment,

        String formatMask,

        String placeHolder,

        @Min(value = 1, message = "Max length must be at least 1")
        Integer maxLength,

        String dropdownCode

) {
}