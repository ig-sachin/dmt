package com.dmt.backend.engine.crud.controller;

import com.dmt.backend.engine.crud.service.GenericCrudService;
import com.dmt.backend.engine.procedure.dto.ProcedureExecutionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class GenericCrudController {

    private final GenericCrudService service;

    @PostMapping("/{screenCode}")
    public ProcedureExecutionResponse insert(
            @PathVariable String screenCode,
            @RequestBody Map<String, Object> values) {

        return service.insert(screenCode, values);
    }

    @PutMapping("/{screenCode}")
    public ProcedureExecutionResponse update(
            @PathVariable String screenCode,
            @RequestBody Map<String, Object> values) {

        return service.update(screenCode, values);
    }

    @DeleteMapping("/{screenCode}")
    public ProcedureExecutionResponse delete(
            @PathVariable String screenCode,
            @RequestBody Map<String, Object> values) {

        return service.delete(screenCode, values);
    }
}