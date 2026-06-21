package com.dmt.backend.engine.crud.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.audit.entity.AuditOperation;
import com.dmt.backend.audit.service.AuditService;
import com.dmt.backend.engine.procedure.dto.ProcedureExecutionRequest;
import com.dmt.backend.engine.procedure.dto.ProcedureExecutionResponse;
import com.dmt.backend.engine.procedure.service.ProcedureEngineService;
import com.dmt.backend.metadata.procedure.entity.OperationType;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import com.dmt.backend.metadata.screenrole.entity.PermissionType;
import com.dmt.backend.security.ScreenAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenericCrudService {

    private final ProcedureEngineService procedureEngineService;
    private final ScreenAuthorizationService authorizationService;
    private final DmtScreenRepository screenRepository;
    private final AuditService auditService;

    public ProcedureExecutionResponse insert(
            String screenCode,
            Map<String, Object> values) {
        return execute(OperationType.INSERT, screenCode, values);
    }

    public ProcedureExecutionResponse update(
            String screenCode,
            Map<String, Object> values) {
        return execute(OperationType.UPDATE, screenCode, values);
    }

    public ProcedureExecutionResponse delete(
            String screenCode,
            Map<String, Object> values) {
        return execute(OperationType.DELETE, screenCode, values);
    }

    private ProcedureExecutionResponse execute(OperationType opType, String screenCode, Map<String, Object> values) {
        authorizationService.authorize(screenCode, mapPermissionType(opType));
        log.info("Generic CRUD authorization successful operationType={} screenCode={}", opType, screenCode);
        log.info("Generic CRUD requested operationType={} screenCode={} valueKeys={}", opType, screenCode, values == null ? null : values.keySet());

        ProcedureExecutionResponse response = procedureEngineService.execute(
                new ProcedureExecutionRequest(screenCode, opType, values)
        );

        if (!response.success()) {
            log.warn(
                    "Generic CRUD audit skipped screenCode={} operationType={} reason=procedure_reported_failure message={}",
                    screenCode,
                    opType,
                    response.message()
            );
            return response;
        }

        DmtScreen screen = screenRepository.findByScreenCode(screenCode)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Screen not found"));

        String primaryKeyColumn = screen.getPrimaryKeyColumn();
        if (primaryKeyColumn == null || primaryKeyColumn.isBlank()) {
            log.warn(
                    "Generic CRUD audit skipped screenCode={} reason=primary_key_metadata_missing",
                    screenCode
            );

            log.info("Generic CRUD completed operationType={} screenCode={} success={} message={}", opType, screenCode, response.success(), response.message());
            return response;
        }

        String recordId = values != null ? String.valueOf(values.getOrDefault(primaryKeyColumn, "UNKNOWN")) : "UNKNOWN";

        if (values != null && !values.isEmpty()) {
            AuditOperation auditOp = mapAuditOp(opType);
            values.forEach((column, value) -> {

                if (column.equalsIgnoreCase(primaryKeyColumn)
                        && opType != OperationType.DELETE) {
                    return;
                }

                String oldValue = null;
                String newValue = null;

                if (opType == OperationType.DELETE) {
                    oldValue = value != null ? String.valueOf(value) : null;
                } else {
                    newValue = value != null ? String.valueOf(value) : null;
                }

                auditService.log(
                        screenCode,
                        recordId,
                        auditOp,
                        column,
                        oldValue,
                        newValue
                );
            });
        }

        log.info("Generic CRUD completed operationType={} screenCode={} success={} message={}", opType, screenCode, response.success(), response.message());
        return response;
    }

    private AuditOperation mapAuditOp(
            OperationType opType) {

        return switch (opType) {

            case INSERT -> AuditOperation.INSERT;

            case UPDATE -> AuditOperation.UPDATE;

            case DELETE -> AuditOperation.DELETE;
        };
    }

    private PermissionType mapPermissionType(OperationType opType) {

        return switch (opType) {

            case INSERT -> PermissionType.INSERT;

            case UPDATE -> PermissionType.UPDATE;

            case DELETE -> PermissionType.DELETE;
        };
    }
}