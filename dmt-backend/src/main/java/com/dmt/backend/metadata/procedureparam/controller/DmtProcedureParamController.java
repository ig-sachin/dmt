package com.dmt.backend.metadata.procedureparam.controller;

import com.dmt.backend.metadata.procedureparam.dto.ProcedureParamRequest;
import com.dmt.backend.metadata.procedureparam.dto.ProcedureParamResponse;
import com.dmt.backend.metadata.procedureparam.service.DmtProcedureParamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procedure-params")
@RequiredArgsConstructor
public class DmtProcedureParamController {

    private final DmtProcedureParamService service;

    @PostMapping
    public ProcedureParamResponse create(
            @Valid @RequestBody ProcedureParamRequest request) {

        return service.create(request);
    }

    @GetMapping("/procedure/{procedureId}")
    public List<ProcedureParamResponse> getByProcedure(
            @PathVariable Long procedureId) {

        return service.getByProcedure(procedureId);
    }
}