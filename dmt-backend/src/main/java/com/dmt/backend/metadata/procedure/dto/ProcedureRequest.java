package com.dmt.backend.metadata.procedure.dto;

import com.dmt.backend.metadata.procedure.entity.OperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProcedureRequest(

        @NotNull(message = "Screen id is required")
        Long screenId,

        @NotNull(message = "Operation type is required")
        OperationType operationType,

        @NotBlank(message = "Procedure name is required")
        @Size(max = 200, message = "Procedure name must be at most 200 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9_.]+$",
                message = "Procedure name may only contain letters, numbers, underscores, and dots")
        String procedureName,

        Boolean active

) {
}