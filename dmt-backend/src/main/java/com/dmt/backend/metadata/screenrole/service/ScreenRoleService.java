package com.dmt.backend.metadata.screenrole.service;

import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import com.dmt.backend.metadata.screenrole.dto.ScreenRoleRequest;
import com.dmt.backend.metadata.screenrole.dto.ScreenRoleResponse;
import com.dmt.backend.metadata.screenrole.entity.DmtScreenRole;
import com.dmt.backend.metadata.screenrole.repository.DmtScreenRoleRepository;
import lombok.RequiredArgsConstructor;
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
                                new RuntimeException(
                                        "Screen not found"));

        DmtScreenRole role =
                DmtScreenRole.builder()
                        .screen(screen)
                        .roleName(request.roleName())
                        .build();

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

        repository.deleteById(id);
    }

    private ScreenRoleResponse map(
            DmtScreenRole role) {

        return new ScreenRoleResponse(
                role.getId(),
                role.getScreen().getId(),
                role.getScreen().getScreenCode(),
                role.getRoleName()
        );
    }
}
