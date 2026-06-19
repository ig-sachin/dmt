package com.dmt.backend.metadata.procedureparam.dto;

public record ProcedureParamResponse(

        Long id,

        Long procedureId,

        String parameterName,

        Integer parameterOrder,

        String columnName,

        String defaultValue,

        Boolean required

) {
}