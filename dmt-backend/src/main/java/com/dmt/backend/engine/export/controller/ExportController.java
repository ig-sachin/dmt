package com.dmt.backend.engine.export.controller;

import com.dmt.backend.engine.export.dto.ExportRequest;
import com.dmt.backend.engine.export.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService service;

    @PostMapping("/{screenCode}/csv")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable String screenCode,
            @RequestBody ExportRequest request) {

        byte[] file =
                service.exportCsv(
                        screenCode,
                        request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename="
                                + screenCode
                                + ".csv")
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        "text/csv")
                .body(file);
    }
}