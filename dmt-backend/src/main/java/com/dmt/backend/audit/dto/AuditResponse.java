package com.dmt.backend.audit.dto;

import com.dmt.backend.audit.entity.AuditOperation;

import java.time.LocalDateTime;

public record AuditResponse(

        Long id,

        String screenCode,

        String recordId,

        AuditOperation operation,

        String columnName,

        String oldValue,

        String newValue,

        String changedBy,

        LocalDateTime createdAt

) {
}