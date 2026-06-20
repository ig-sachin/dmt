package com.dmt.backend.metadata.dropdownparam.controller;

import com.dmt.backend.metadata.dropdownparam.dto.DropdownParamRequest;
import com.dmt.backend.metadata.dropdownparam.dto.DropdownParamResponse;
import com.dmt.backend.metadata.dropdownparam.service.DropdownParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dropdown-params")
@RequiredArgsConstructor
public class DropdownParamController {

    private final DropdownParamService service;

    @PostMapping
    public DropdownParamResponse create(
            @RequestBody DropdownParamRequest request) {

        return service.create(request);
    }

    @GetMapping("/{dropdownId}")
    public List<DropdownParamResponse> get(
            @PathVariable Long dropdownId) {

        return service.getByDropdown(
                dropdownId);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id) {

        service.delete(id);
    }
}