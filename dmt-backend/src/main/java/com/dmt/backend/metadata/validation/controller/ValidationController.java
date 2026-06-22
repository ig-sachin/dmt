package com.dmt.backend.metadata.validation.controller;

import com.dmt.backend.metadata.validation.dto.ValidationRequest;
import com.dmt.backend.metadata.validation.dto.ValidationResponse;
import com.dmt.backend.metadata.validation.service.ValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/validations")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService service;

    @PostMapping
    public ValidationResponse create(
            @Valid @RequestBody ValidationRequest request) {

        return service.create(request);
    }

    @GetMapping("/{columnId}")
    public List<ValidationResponse> getByColumn(
            @PathVariable Long columnId) {

        return service.getByColumn(columnId);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id) {

        service.delete(id);
    }
}