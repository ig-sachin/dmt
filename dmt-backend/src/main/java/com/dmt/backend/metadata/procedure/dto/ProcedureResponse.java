package com.dmt.backend.metadata.procedure.dto;

import com.dmt.backend.metadata.procedure.entity.OperationType;

public record ProcedureResponse(

        Long id,

        Long screenId,

        OperationType operationType,

        String procedureName,

        Boolean active

) {
}