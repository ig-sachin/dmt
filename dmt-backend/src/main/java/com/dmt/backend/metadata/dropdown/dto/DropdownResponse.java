package com.dmt.backend.metadata.dropdown.dto;

public record DropdownResponse(

        Long id,

        String dropdownCode,

        String dropdownName,

        String query,

        Boolean active

) {
}