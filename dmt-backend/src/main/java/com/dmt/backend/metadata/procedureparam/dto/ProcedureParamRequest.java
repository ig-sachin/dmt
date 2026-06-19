package com.dmt.backend.metadata.procedureparam.dto;

public record ProcedureParamRequest(

        Long procedureId,

        String parameterName,

        Integer parameterOrder,

        String columnName,

        String defaultValue,

        Boolean required

) {
}