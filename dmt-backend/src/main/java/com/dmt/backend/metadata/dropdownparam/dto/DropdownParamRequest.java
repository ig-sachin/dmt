package com.dmt.backend.metadata.dropdownparam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DropdownParamRequest(

        @NotNull(message = "Dropdown id is required")
        Long dropdownId,

        @NotBlank(message = "Parameter name is required")
        String parameterName,

        @NotBlank(message = "Request field is required")
        String requestField,

        Boolean required,

        @Min(value = 1, message = "Parameter order must be at least 1")
        Integer parameterOrder

) {
}