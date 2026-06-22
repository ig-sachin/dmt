package com.dmt.backend.metadata.column.controller;

import com.dmt.backend.metadata.column.dto.ColumnRequest;
import com.dmt.backend.metadata.column.dto.ColumnResponse;
import com.dmt.backend.metadata.column.service.DmtColumnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/columns")
@RequiredArgsConstructor
public class DmtColumnController {

    private final DmtColumnService service;

    @PostMapping
    public ColumnResponse create(
            @Valid @RequestBody ColumnRequest request) {

        return service.create(request);
    }

    @GetMapping("/screen/{screenId}")
    public List<ColumnResponse> getByScreen(
            @PathVariable Long screenId) {

        return service.getByScreen(screenId);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id) {

        service.delete(id);
    }
}