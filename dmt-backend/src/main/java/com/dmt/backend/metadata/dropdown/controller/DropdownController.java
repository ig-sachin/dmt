package com.dmt.backend.metadata.dropdown.controller;

import com.dmt.backend.metadata.dropdown.dto.DropdownRequest;
import com.dmt.backend.metadata.dropdown.dto.DropdownResponse;
import com.dmt.backend.metadata.dropdown.service.DropdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dropdowns")
@RequiredArgsConstructor
public class DropdownController {

    private final DropdownService service;

    @PostMapping
    public DropdownResponse create(
            @RequestBody DropdownRequest request) {

        return service.create(request);
    }

    @GetMapping
    public List<DropdownResponse> getAll() {

        return service.getAll();
    }

    @GetMapping("/{id}")
    public DropdownResponse getById(
            @PathVariable Long id) {

        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id) {

        service.delete(id);
    }
}