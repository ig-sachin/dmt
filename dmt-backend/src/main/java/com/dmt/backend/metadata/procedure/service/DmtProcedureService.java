package com.dmt.backend.metadata.procedure.service;

import com.dmt.backend.metadata.procedure.dto.ProcedureRequest;
import com.dmt.backend.metadata.procedure.dto.ProcedureResponse;
import com.dmt.backend.metadata.procedure.entity.DmtProcedure;
import com.dmt.backend.metadata.procedure.repository.DmtProcedureRepository;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DmtProcedureService {

    private final DmtProcedureRepository procedureRepository;
    private final DmtScreenRepository screenRepository;

    public ProcedureResponse create(
            ProcedureRequest request) {

        log.info(
                "Create procedure requested screenId={} operationType={} procedureName={}",
                request.screenId(),
                request.operationType(),
                request.procedureName()
        );

        DmtScreen screen =
                screenRepository.findById(
                                request.screenId())
                        .orElseThrow(() -> {
                            log.warn("Create procedure failed screenId={} reason=screen_not_found", request.screenId());
                            return new RuntimeException(
                                    "Screen not found");
                        });

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

        DmtProcedure saved = procedureRepository.save(procedure);

        log.info(
                "Procedure created id={} screenId={} operationType={} procedureName={}",
                saved.getId(),
                request.screenId(),
                request.operationType(),
                request.procedureName()
        );

        return map(saved);
    }

    public List<ProcedureResponse> getByScreen(
            Long screenId) {

        List<ProcedureResponse> procedures = procedureRepository
                .findByScreenId(screenId)
                .stream()
                .map(this::map)
                .toList();

        log.info("Procedures fetched screenId={} count={}", screenId, procedures.size());

        return procedures;
    }

    public void delete(Long id) {

        procedureRepository.deleteById(id);

        log.info("Procedure deleted id={}", id);
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
