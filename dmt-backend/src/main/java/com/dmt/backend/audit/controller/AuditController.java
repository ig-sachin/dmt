package com.dmt.backend.audit.controller;

import com.dmt.backend.audit.dto.AuditResponse;
import com.dmt.backend.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService service;

    @GetMapping("/{screenCode}")
    public List<AuditResponse> getByScreen(
            @PathVariable String screenCode) {

        return service.getByScreen(
                screenCode);
    }

    @GetMapping("/{screenCode}/{recordId}")
    public List<AuditResponse> getByRecord(
            @PathVariable String screenCode,
            @PathVariable String recordId) {

        return service.getByRecord(
                screenCode,
                recordId);
    }
}