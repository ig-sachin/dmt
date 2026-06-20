package com.dmt.backend.engine.form.dto;

import java.util.List;

public record FormFieldResponse(

        String columnName,

        String displayName,

        String dataType,

        String fieldType,

        Boolean visible,

        Boolean editable,

        Boolean mandatory,

        String defaultValue,

        Integer displayOrder,

        Integer width,

        String alignment,

        String formatMask,

        String placeholder,

        Integer maxLength,

        String dropdownCode,

        List<FormValidationResponse> validations

) {
}