package com.dmt.backend.metadata.screenrole.controller;

import com.dmt.backend.metadata.screenrole.dto.ScreenRoleRequest;
import com.dmt.backend.metadata.screenrole.dto.ScreenRoleResponse;
import com.dmt.backend.metadata.screenrole.service.ScreenRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screen-roles")
@RequiredArgsConstructor
public class ScreenRoleController {

    private final ScreenRoleService service;

    @PostMapping
    public ScreenRoleResponse create(
            @RequestBody ScreenRoleRequest request) {

        return service.create(request);
    }

    @PutMapping("/{id}")
    public ScreenRoleResponse update(
            @PathVariable Long id,
            @RequestBody ScreenRoleRequest request) {

        return service.update(id, request);
    }

    @GetMapping("/{screenCode}")
    public List<ScreenRoleResponse> get(
            @PathVariable String screenCode) {

        return service.getByScreen(screenCode);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id) {

        service.delete(id);
    }
}