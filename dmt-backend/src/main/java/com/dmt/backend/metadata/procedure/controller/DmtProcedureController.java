package com.dmt.backend.metadata.procedure.controller;

import com.dmt.backend.metadata.procedure.dto.ProcedureRequest;
import com.dmt.backend.metadata.procedure.dto.ProcedureResponse;
import com.dmt.backend.metadata.procedure.service.DmtProcedureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procedures")
@RequiredArgsConstructor
public class DmtProcedureController {

    private final DmtProcedureService service;

    @PostMapping
    public ProcedureResponse create(
            @Valid @RequestBody ProcedureRequest request) {

        return service.create(request);
    }

    @GetMapping("/screen/{screenId}")
    public List<ProcedureResponse> getByScreen(
            @PathVariable Long screenId) {

        return service.getByScreen(screenId);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id) {

        service.delete(id);
    }
}