package com.dmt.backend.engine.procedure.service;

import com.dmt.backend.engine.procedure.dto.ProcedureExecutionRequest;
import com.dmt.backend.engine.procedure.dto.ProcedureExecutionResponse;
import com.dmt.backend.metadata.procedure.entity.DmtProcedure;
import com.dmt.backend.metadata.procedure.entity.OperationType;
import com.dmt.backend.metadata.procedure.repository.DmtProcedureRepository;
import com.dmt.backend.metadata.procedureparam.entity.DmtProcedureParam;
import com.dmt.backend.metadata.procedureparam.repository.DmtProcedureParamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcedureEngineService {

    private final JdbcTemplate jdbcTemplate;
    private final DmtProcedureRepository procedureRepository;
    private final DmtProcedureParamRepository paramRepository;

    public ProcedureExecutionResponse execute(
            ProcedureExecutionRequest request) {
        System.out.println(
                "Requested Operation = "
                        + request.operationType());
        DmtProcedure procedure =
                procedureRepository
                        .findByScreenScreenCodeAndOperationTypeAndActiveTrue(
                                request.screenCode(),
                                OperationType.valueOf(String.valueOf(request.operationType())))
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Procedure not found"));
        System.out.println(
                "Resolved Procedure = "
                        + procedure.getProcedureName());
        List<DmtProcedureParam> params =
                paramRepository
                        .findByProcedureIdOrderByParameterOrderAsc(
                                procedure.getId());
        System.out.println(
                "Procedure ID = " + procedure.getId());

        System.out.println(
                "Param Count = " + params.size());

        params.forEach(p ->
                System.out.println(
                        p.getParameterName()
                                + " -> "
                                + p.getColumnName()));
        jdbcTemplate.execute((Connection connection) -> {

            StringBuilder call =
                    new StringBuilder("{call ");

            call.append(procedure.getProcedureName());

            call.append("(");

            for (int i = 0; i < params.size(); i++) {

                call.append("?");

                if (i < params.size() - 1) {
                    call.append(",");
                }
            }

            call.append(")}");

            CallableStatement cs =
                    connection.prepareCall(
                            call.toString());

            System.out.println("Executing Procedure = " + procedure.getProcedureName());

            int index = 1;
            System.out.println(
                    "Request Values = "
                            + request.values());

            params.forEach(p ->
                    System.out.println(
                            "Column Metadata = "
                                    + p.getColumnName()));
            for (DmtProcedureParam param : params) {

                Object value =
                        request.values()
                                .get(param.getColumnName());

                if (value == null) {

                    value =
                            param.getDefaultValue();
                }

                System.out.println(
                        "Binding "
                                + param.getParameterName()
                                + " = "
                                + value);

                cs.setObject(index++, value);
            }

            cs.execute();

            return null;
        });

        return new ProcedureExecutionResponse(
                true,
                "Procedure executed successfully");
    }
}