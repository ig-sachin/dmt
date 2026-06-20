package com.dmt.backend.metadata.validation.dto;

import com.dmt.backend.metadata.validation.entity.ValidationType;

public record ValidationRequest(

        Long columnId,

        ValidationType validationType,

        String validationValue,

        String errorMessage,

        Boolean active

) {
}