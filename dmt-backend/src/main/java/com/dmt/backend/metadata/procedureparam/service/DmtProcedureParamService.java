package com.dmt.backend.metadata.procedureparam.service;

import com.dmt.backend.metadata.procedure.entity.DmtProcedure;
import com.dmt.backend.metadata.procedure.repository.DmtProcedureRepository;
import com.dmt.backend.metadata.procedureparam.dto.ProcedureParamRequest;
import com.dmt.backend.metadata.procedureparam.dto.ProcedureParamResponse;
import com.dmt.backend.metadata.procedureparam.entity.DmtProcedureParam;
import com.dmt.backend.metadata.procedureparam.repository.DmtProcedureParamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DmtProcedureParamService {

    private final DmtProcedureParamRepository repository;
    private final DmtProcedureRepository procedureRepository;

    public ProcedureParamResponse create(
            ProcedureParamRequest request) {

        DmtProcedure procedure =
                procedureRepository.findById(
                                request.procedureId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Procedure not found"));

        DmtProcedureParam param =
                DmtProcedureParam.builder()
                        .procedure(procedure)
                        .parameterName(
                                request.parameterName())
                        .parameterOrder(
                                request.parameterOrder())
                        .columnName(
                                request.columnName())
                        .defaultValue(
                                request.defaultValue())
                        .required(
                                request.required())
                        .build();

        param = repository.save(param);

        return map(param);
    }

    public List<ProcedureParamResponse> getByProcedure(
            Long procedureId) {

        return repository
                .findByProcedureIdOrderByParameterOrderAsc(
                        procedureId)
                .stream()
                .map(this::map)
                .toList();
    }

    private ProcedureParamResponse map(
            DmtProcedureParam param) {

        return new ProcedureParamResponse(
                param.getId(),
                param.getProcedure().getId(),
                param.getParameterName(),
                param.getParameterOrder(),
                param.getColumnName(),
                param.getDefaultValue(),
                param.getRequired()
        );
    }
}