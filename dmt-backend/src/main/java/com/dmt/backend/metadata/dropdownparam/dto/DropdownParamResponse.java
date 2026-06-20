package com.dmt.backend.metadata.dropdownparam.dto;

public record DropdownParamResponse(

        Long id,

        Long dropdownId,

        String parameterName,

        String requestField,

        Boolean required,

        Integer parameterOrder

) {
}