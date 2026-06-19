package com.dmt.backend.metadata.procedure.dto;

import com.dmt.backend.metadata.procedure.entity.OperationType;

public record ProcedureRequest(

        Long screenId,

        OperationType operationType,

        String procedureName,

        Boolean active

) {
}