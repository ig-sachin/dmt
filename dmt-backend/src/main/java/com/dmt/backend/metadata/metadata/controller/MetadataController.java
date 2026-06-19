package com.dmt.backend.metadata.metadata.controller;

import com.dmt.backend.metadata.metadata.dto.MetadataResponse;
import com.dmt.backend.metadata.metadata.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataService service;

    @GetMapping("/{screenCode}")
    public MetadataResponse getMetadata(
            @PathVariable String screenCode) {

        return service.getMetadata(screenCode);
    }
}