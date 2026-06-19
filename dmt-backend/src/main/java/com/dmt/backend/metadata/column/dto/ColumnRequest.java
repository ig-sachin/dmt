package com.dmt.backend.metadata.column.dto;

import com.dmt.backend.metadata.column.entity.FieldType;

public record ColumnRequest(

        Long screenId,

        String columnName,

        String displayName,

        String dataType,

        FieldType fieldType,

        Boolean visible,

        Boolean editable,

        Boolean mandatory,

        String defaultValue,

        Integer displayOrder,

        Integer width,

        String alignment,

        String formatMask

) {
}