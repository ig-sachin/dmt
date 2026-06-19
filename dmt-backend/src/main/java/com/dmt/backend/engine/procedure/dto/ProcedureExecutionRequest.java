package com.dmt.backend.engine.procedure.dto;

import com.dmt.backend.metadata.procedure.entity.OperationType;

import java.util.Map;

public record ProcedureExecutionRequest(

        String screenCode,

        OperationType operationType,

        Map<String, Object> values

) {
}