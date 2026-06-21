package com.dmt.backend.engine.procedure.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.engine.procedure.dto.ProcedureExecutionRequest;
import com.dmt.backend.engine.procedure.dto.ProcedureExecutionResponse;
import com.dmt.backend.metadata.procedure.entity.DmtProcedure;
import com.dmt.backend.metadata.procedure.entity.OperationType;
import com.dmt.backend.metadata.procedure.repository.DmtProcedureRepository;
import com.dmt.backend.metadata.procedureparam.entity.DmtProcedureParam;
import com.dmt.backend.metadata.procedureparam.repository.DmtProcedureParamRepository;
import com.dmt.backend.metadata.screenrole.entity.PermissionType;
import com.dmt.backend.security.ScreenAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcedureEngineService {

    private final JdbcTemplate jdbcTemplate;
    private final DmtProcedureRepository procedureRepository;
    private final DmtProcedureParamRepository paramRepository;
    private final ScreenAuthorizationService authorizationService;

    public ProcedureExecutionResponse execute(
            ProcedureExecutionRequest request) {

        authorizationService.authorize(request.screenCode(), mapPermissionType(request.operationType()));
        log.info(
                "Procedure authorization successful screenCode={} operationType={}",
                request.screenCode(),
                request.operationType()
        );

        log.info(
                "Procedure execution requested screenCode={} operationType={} valueKeys={}",
                request.screenCode(),
                request.operationType(),
                request.values() == null ? null : request.values().keySet()
        );

        DmtProcedure procedure =
                procedureRepository
                        .findByScreenScreenCodeAndOperationTypeAndActiveTrue(
                                request.screenCode(),
                                request.operationType())
                        .orElseThrow(() -> {
                            log.warn(
                                    "Procedure not found screenCode={} operationType={}",
                                    request.screenCode(),
                                    request.operationType()
                            );

                            return new ApiException(
                                    HttpStatus.BAD_REQUEST,
                                    "Procedure not found");
                        });

        List<DmtProcedureParam> params =
                paramRepository
                        .findByProcedureIdOrderByParameterOrderAsc(
                                procedure.getId());

        log.info(
                "Resolved procedure procedureName={} procedureId={} paramCount={}",
                procedure.getProcedureName(),
                procedure.getId(),
                params.size()
        );

        try {
            jdbcTemplate.execute((Connection connection) -> {

                String callSql = buildCallSql(
                        procedure.getProcedureName(),
                        params.size());

                CallableStatement callableStatement =
                        connection.prepareCall(callSql);

                int index = 1;

                for (DmtProcedureParam param : params) {

                    Object value =
                            request.values() == null
                                    ? null
                                    : request.values()
                                    .get(param.getColumnName());

                    if (value == null) {
                        value = param.getDefaultValue();
                    }

                    if (Boolean.TRUE.equals(param.getRequired())
                            && value == null) {

                        log.warn(
                                "Procedure validation failed procedureName={} missingColumn={}",
                                procedure.getProcedureName(),
                                param.getColumnName()
                        );

                        throw new ApiException(
                                HttpStatus.BAD_REQUEST,
                                "Missing required value for: "
                                        + param.getColumnName());
                    }

                    callableStatement.setObject(index++, value);
                }

                callableStatement.execute();

                log.info(
                        "Procedure executed procedureName={} screenCode={} operationType={}",
                        procedure.getProcedureName(),
                        request.screenCode(),
                        request.operationType()
                );

                return null;
            });
        } catch (DataAccessException dataAccessException) {

            String dbMessage = extractDatabaseMessage(dataAccessException);

            log.warn(
                    "Procedure execution rejected by database procedureName={} screenCode={} operationType={} message={}",
                    procedure.getProcedureName(),
                    request.screenCode(),
                    request.operationType(),
                    dbMessage
            );

            return new ProcedureExecutionResponse(
                    false,
                    dbMessage);
        }

        return new ProcedureExecutionResponse(
                true,
                "Procedure executed successfully");
    }

    /**
     * Stored procedures raise business-rule failures via RAISE_APPLICATION_ERROR (or
     * equivalent), which JDBC surfaces as a SQLException wrapped inside a Spring
     * DataAccessException. We unwrap down to the root SQLException and return its
     * message, which is the actual text the database/procedure intended the caller
     * to see (e.g. "ORA-20001: Duplicate customer code CUST-0042").
     */
    private String extractDatabaseMessage(DataAccessException dataAccessException) {

        Throwable cause = dataAccessException;

        while (cause.getCause() != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
        }

        if (cause instanceof SQLException sqlException) {
            String message = sqlException.getMessage();
            return message != null && !message.isBlank()
                    ? message
                    : "The database rejected the operation.";
        }

        String message = dataAccessException.getMostSpecificCause() != null
                ? dataAccessException.getMostSpecificCause().getMessage()
                : dataAccessException.getMessage();

        return message != null && !message.isBlank()
                ? message
                : "The database rejected the operation.";
    }

    private String buildCallSql(
            String procedureName,
            int parameterCount) {

        StringBuilder call =
                new StringBuilder("{call ");

        call.append(procedureName);
        call.append("(");

        for (int i = 0; i < parameterCount; i++) {
            call.append("?");

            if (i < parameterCount - 1) {
                call.append(",");
            }
        }

        call.append(")}");

        return call.toString();
    }

    private PermissionType mapPermissionType(OperationType opType) {

        return switch (opType) {

            case INSERT -> PermissionType.INSERT;

            case UPDATE -> PermissionType.UPDATE;

            case DELETE -> PermissionType.DELETE;
        };
    }
}