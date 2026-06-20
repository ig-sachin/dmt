package com.dmt.backend.engine.dropdown.controller;

import com.dmt.backend.engine.dropdown.dto.DropdownOptionResponse;
import com.dmt.backend.engine.dropdown.service.DropdownEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dropdowns")
@RequiredArgsConstructor
public class DropdownEngineController {

    private final DropdownEngineService service;

    @GetMapping("/{dropdownCode}/options")
    public List<DropdownOptionResponse> getOptions(
            @PathVariable String dropdownCode,
            @RequestParam Map<String,String> params) {

        return service.getOptions(
                dropdownCode,
                params);
    }
}