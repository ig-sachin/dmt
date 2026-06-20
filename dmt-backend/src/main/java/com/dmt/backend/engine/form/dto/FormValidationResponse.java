package com.dmt.backend.engine.form.dto;

public record FormValidationResponse(

        String validationType,

        String validationValue,

        String errorMessage

) {
}