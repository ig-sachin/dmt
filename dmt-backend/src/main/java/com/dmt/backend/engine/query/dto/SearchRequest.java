package com.dmt.backend.engine.query.dto;

import java.util.Map;

public record SearchRequest(

        Integer page,

        Integer size,

        String sortColumn,

        String sortDirection,

        Map<String, Object> filters

) {
}