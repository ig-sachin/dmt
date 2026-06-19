package com.dmt.backend.engine.query.dto;

import java.util.List;
import java.util.Map;

public record SearchResponse(

        List<Map<String, Object>> content,

        Long totalRecords,

        Integer totalPages,

        Integer page,

        Integer size

) {
}