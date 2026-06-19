package com.dmt.backend.engine.crud.service;

import com.dmt.backend.engine.procedure.dto.ProcedureExecutionRequest;
import com.dmt.backend.engine.procedure.dto.ProcedureExecutionResponse;
import com.dmt.backend.engine.procedure.service.ProcedureEngineService;
import com.dmt.backend.metadata.procedure.entity.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenericCrudService {

    private final ProcedureEngineService procedureEngineService;

    public ProcedureExecutionResponse insert(
            String screenCode,
            Map<String, Object> values) {

        return procedureEngineService.execute(
                new ProcedureExecutionRequest(
                        screenCode,
                        OperationType.INSERT,
                        values
                )
        );
    }

    public ProcedureExecutionResponse update(
            String screenCode,
            Map<String, Object> values) {

        return procedureEngineService.execute(
                new ProcedureExecutionRequest(
                        screenCode,
                        OperationType.UPDATE,
                        values
                )
        );
    }

    public ProcedureExecutionResponse delete(
            String screenCode,
            Map<String, Object> values) {

        return procedureEngineService.execute(
                new ProcedureExecutionRequest(
                        screenCode,
                        OperationType.DELETE,
                        values
                )
        );
    }
}