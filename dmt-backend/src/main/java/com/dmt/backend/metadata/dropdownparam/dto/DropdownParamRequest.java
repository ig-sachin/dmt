package com.dmt.backend.metadata.dropdownparam.dto;

public record DropdownParamRequest(

        Long dropdownId,

        String parameterName,

        String requestField,

        Boolean required,

        Integer parameterOrder

) {
}