package com.dmt.backend.metadata.procedureparam.service;

import com.dmt.backend.metadata.procedure.entity.DmtProcedure;
import com.dmt.backend.metadata.procedure.repository.DmtProcedureRepository;
import com.dmt.backend.metadata.procedureparam.dto.ProcedureParamRequest;
import com.dmt.backend.metadata.procedureparam.dto.ProcedureParamResponse;
import com.dmt.backend.metadata.procedureparam.entity.DmtProcedureParam;
import com.dmt.backend.metadata.procedureparam.repository.DmtProcedureParamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DmtProcedureParamService {

    private final DmtProcedureParamRepository repository;
    private final DmtProcedureRepository procedureRepository;

    public ProcedureParamResponse create(
            ProcedureParamRequest request) {

        log.info(
                "Create procedure parameter requested procedureId={} parameterName={} columnName={}",
                request.procedureId(),
                request.parameterName(),
                request.columnName()
        );

        DmtProcedure procedure =
                procedureRepository.findById(
                                request.procedureId())
                        .orElseThrow(() -> {
                            log.warn("Create procedure parameter failed procedureId={} reason=procedure_not_found", request.procedureId());
                            return new RuntimeException(
                                    "Procedure not found");
                        });

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

        log.info(
                "Procedure parameter created id={} procedureId={} columnName={}",
                param.getId(),
                request.procedureId(),
                request.columnName()
        );

        return map(param);
    }

    public List<ProcedureParamResponse> getByProcedure(
            Long procedureId) {

        List<ProcedureParamResponse> params = repository
                .findByProcedureIdOrderByParameterOrderAsc(
                        procedureId)
                .stream()
                .map(this::map)
                .toList();

        log.info("Procedure parameters fetched procedureId={} count={}", procedureId, params.size());

        return params;
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
