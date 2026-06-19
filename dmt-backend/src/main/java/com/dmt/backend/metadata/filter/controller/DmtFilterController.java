package com.dmt.backend.metadata.filter.controller;

import com.dmt.backend.metadata.filter.dto.FilterRequest;
import com.dmt.backend.metadata.filter.dto.FilterResponse;
import com.dmt.backend.metadata.filter.service.DmtFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/filters")
@RequiredArgsConstructor
public class DmtFilterController {

    private final DmtFilterService service;

    @PostMapping
    public FilterResponse create(
            @RequestBody FilterRequest request) {

        return service.create(request);
    }

    @GetMapping("/screen/{screenId}")
    public List<FilterResponse> getByScreen(
            @PathVariable Long screenId) {

        return service.getByScreen(screenId);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id) {

        service.delete(id);
    }
}