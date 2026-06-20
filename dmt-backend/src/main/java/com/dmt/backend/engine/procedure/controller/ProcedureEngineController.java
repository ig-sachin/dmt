package com.dmt.backend.engine.procedure.controller;

import com.dmt.backend.engine.procedure.dto.ProcedureExecutionRequest;
import com.dmt.backend.engine.procedure.dto.ProcedureExecutionResponse;
import com.dmt.backend.engine.procedure.service.ProcedureEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procedure-engine")
@RequiredArgsConstructor
public class ProcedureEngineController {

    private final ProcedureEngineService service;

    @PostMapping("/execute")
    public ProcedureExecutionResponse execute(
            @RequestBody ProcedureExecutionRequest request) {

        return service.execute(request);
    }
}