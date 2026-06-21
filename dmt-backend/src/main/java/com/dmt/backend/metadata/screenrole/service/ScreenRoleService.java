package com.dmt.backend.metadata.screenrole.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import com.dmt.backend.metadata.screenrole.dto.ScreenRoleRequest;
import com.dmt.backend.metadata.screenrole.dto.ScreenRoleResponse;
import com.dmt.backend.metadata.screenrole.entity.DmtScreenRole;
import com.dmt.backend.metadata.screenrole.repository.DmtScreenRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenRoleService {

    private final DmtScreenRoleRepository repository;
    private final DmtScreenRepository screenRepository;

    public ScreenRoleResponse create(
            ScreenRoleRequest request) {

        DmtScreen screen =
                screenRepository
                        .findById(request.screenId())
                        .orElseThrow(() ->
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Screen not found"));

        DmtScreenRole role =
                DmtScreenRole.builder()
                        .screen(screen)
                        .roleName(request.roleName())
                        .canView(defaultTrue(request.canView()))
                        .canInsert(defaultTrue(request.canInsert()))
                        .canUpdate(defaultTrue(request.canUpdate()))
                        .canDelete(defaultTrue(request.canDelete()))
                        .build();

        role = repository.save(role);

        return map(role);
    }

    public ScreenRoleResponse update(
            Long id,
            ScreenRoleRequest request) {

        DmtScreenRole role =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Screen role mapping not found"));

        if (request.roleName() != null) {
            role.setRoleName(request.roleName());
        }
        if (request.canView() != null) {
            role.setCanView(request.canView());
        }
        if (request.canInsert() != null) {
            role.setCanInsert(request.canInsert());
        }
        if (request.canUpdate() != null) {
            role.setCanUpdate(request.canUpdate());
        }
        if (request.canDelete() != null) {
            role.setCanDelete(request.canDelete());
        }

        role = repository.save(role);

        return map(role);
    }

    @Transactional(readOnly = true)
    public List<ScreenRoleResponse>
    getByScreen(String screenCode) {

        return repository
                .findByScreenScreenCode(screenCode)
                .stream()
                .map(this::map)
                .toList();
    }

    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Screen role mapping not found");
        }

        repository.deleteById(id);
    }

    private boolean defaultTrue(Boolean value) {
        return value == null || value;
    }

    private ScreenRoleResponse map(
            DmtScreenRole role) {

        return new ScreenRoleResponse(
                role.getId(),
                role.getScreen().getId(),
                role.getScreen().getScreenCode(),
                role.getRoleName(),
                role.getCanView(),
                role.getCanInsert(),
                role.getCanUpdate(),
                role.getCanDelete()
        );
    }
}