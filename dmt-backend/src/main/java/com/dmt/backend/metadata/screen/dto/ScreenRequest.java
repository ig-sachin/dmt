package com.dmt.backend.metadata.screen.dto;

public record ScreenRequest(
        String screenCode,
        String screenName,
        String description,
        String selectQuery,
        Integer defaultPageSize,
        String defaultSortColumn,
        String defaultSortDirection,
        Boolean active,
        String primaryKeyColumn
) {
}