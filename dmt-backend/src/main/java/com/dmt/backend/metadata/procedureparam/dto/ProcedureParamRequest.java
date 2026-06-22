package com.dmt.backend.metadata.procedureparam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProcedureParamRequest(

        @NotNull(message = "Procedure id is required")
        Long procedureId,

        @NotBlank(message = "Parameter name is required")
        String parameterName,

        @NotNull(message = "Parameter order is required")
        @Min(value = 1, message = "Parameter order must be at least 1")
        Integer parameterOrder,

        @NotBlank(message = "Column name is required")
        String columnName,

        String defaultValue,

        Boolean required

) {
}