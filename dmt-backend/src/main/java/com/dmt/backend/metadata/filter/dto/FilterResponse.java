package com.dmt.backend.metadata.filter.dto;

import com.dmt.backend.metadata.filter.entity.FilterType;

public record FilterResponse(

        Long id,

        Long screenId,

        String filterName,

        String columnName,

        FilterType filterType,

        Boolean required,

        String defaultValue,

        Integer displayOrder,

        String dropdownQuery

) {
}