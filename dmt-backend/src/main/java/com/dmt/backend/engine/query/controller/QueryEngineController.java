package com.dmt.backend.engine.query.controller;

import com.dmt.backend.engine.query.dto.SearchRequest;
import com.dmt.backend.engine.query.dto.SearchResponse;
import com.dmt.backend.engine.query.service.QueryEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class QueryEngineController {

    private final QueryEngineService service;

    @PostMapping("/{screenCode}/search")
    public SearchResponse search(
            @PathVariable String screenCode,
            @RequestBody SearchRequest request) {

        return service.search(
                screenCode,
                request
        );
    }
}