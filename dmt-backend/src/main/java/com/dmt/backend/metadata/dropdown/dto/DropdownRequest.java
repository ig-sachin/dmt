package com.dmt.backend.metadata.dropdown.dto;

public record DropdownRequest(

        String dropdownCode,

        String dropdownName,

        String query,

        Boolean active

) {
}