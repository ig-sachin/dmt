package com.dmt.backend.metadata.validation.dto;

import com.dmt.backend.metadata.validation.entity.ValidationType;

public record ValidationResponse(

        Long id,

        Long columnId,

        ValidationType validationType,

        String validationValue,

        String errorMessage,

        Boolean active

) {
}