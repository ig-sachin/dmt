package com.dmt.backend.metadata.procedure.service;

import com.dmt.backend.metadata.procedure.dto.ProcedureRequest;
import com.dmt.backend.metadata.procedure.dto.ProcedureResponse;
import com.dmt.backend.metadata.procedure.entity.DmtProcedure;
import com.dmt.backend.metadata.procedure.repository.DmtProcedureRepository;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DmtProcedureService {

    private final DmtProcedureRepository procedureRepository;
    private final DmtScreenRepository screenRepository;

    public ProcedureResponse create(
            ProcedureRequest request) {

        DmtScreen screen =
                screenRepository.findById(
                                request.screenId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Screen not found"));

        DmtProcedure procedure =
                DmtProcedure.builder()
                        .screen(screen)
                        .operationType(
                                request.operationType())
                        .procedureName(
                                request.procedureName())
                        .active(
                                request.active())
                        .build();

        return map(
                procedureRepository.save(
                        procedure));
    }

    public List<ProcedureResponse> getByScreen(
            Long screenId) {

        return procedureRepository
                .findByScreenId(screenId)
                .stream()
                .map(this::map)
                .toList();
    }

    public void delete(Long id) {

        procedureRepository.deleteById(id);
    }

    private ProcedureResponse map(
            DmtProcedure procedure) {

        return new ProcedureResponse(
                procedure.getId(),
                procedure.getScreen().getId(),
                procedure.getOperationType(),
                procedure.getProcedureName(),
                procedure.getActive()
        );
    }
}