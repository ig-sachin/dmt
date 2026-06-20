package com.dmt.backend.engine.form.controller;

import com.dmt.backend.engine.form.dto.FormResponse;
import com.dmt.backend.engine.form.service.FormEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormEngineController {

    private final FormEngineService service;

    @GetMapping("/{screenCode}")
    public FormResponse getForm(
            @PathVariable String screenCode) {

        return service.getForm(screenCode);
    }
}