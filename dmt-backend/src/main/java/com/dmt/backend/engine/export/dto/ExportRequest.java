package com.dmt.backend.engine.export.dto;

import java.util.Map;

public record ExportRequest(

        Map<String, Object> filters,

        String sortColumn,

        String sortDirection

) {
}