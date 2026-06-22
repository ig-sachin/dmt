package com.dmt.backend.metadata.validation.dto;

import com.dmt.backend.metadata.validation.entity.ValidationType;
import jakarta.validation.constraints.NotNull;

public record ValidationRequest(

        @NotNull(message = "Column id is required")
        Long columnId,

        @NotNull(message = "Validation type is required")
        ValidationType validationType,

        String validationValue,

        String errorMessage,

        Boolean active

) {
}