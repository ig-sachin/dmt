package com.dmt.backend.metadata.metadata.dto;

import com.dmt.backend.metadata.column.dto.ColumnResponse;
import com.dmt.backend.metadata.filter.dto.FilterResponse;
import com.dmt.backend.metadata.screen.dto.ScreenResponse;

import java.util.List;

public record MetadataResponse(

        ScreenResponse screen,

        List<ColumnResponse> columns,

        List<FilterResponse> filters

) {
}