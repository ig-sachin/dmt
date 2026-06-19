package com.dmt.backend.metadata.filter.dto;

import com.dmt.backend.metadata.filter.entity.FilterType;

public record FilterRequest(

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