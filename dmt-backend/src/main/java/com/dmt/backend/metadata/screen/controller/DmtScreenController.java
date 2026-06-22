package com.dmt.backend.metadata.screen.controller;

import com.dmt.backend.metadata.screen.dto.ScreenRequest;
import com.dmt.backend.metadata.screen.dto.ScreenResponse;
import com.dmt.backend.metadata.screen.service.DmtScreenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screens")
@RequiredArgsConstructor
public class DmtScreenController {

    private final DmtScreenService service;

    @PostMapping
    public ScreenResponse create(
            @Valid @RequestBody ScreenRequest request) {

        return service.create(request);
    }

    @GetMapping
    public List<ScreenResponse> getAll() {

        return service.getAll();
    }

    @GetMapping("/{id}")
    public ScreenResponse getById(
            @PathVariable Long id) {

        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id) {

        service.delete(id);
    }

    @DeleteMapping("/{id}/permanent")
    public void hardDelete(
            @PathVariable Long id) {

        service.hardDelete(id);
    }
}