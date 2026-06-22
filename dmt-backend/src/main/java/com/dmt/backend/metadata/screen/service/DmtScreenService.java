package com.dmt.backend.metadata.screen.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.filter.repository.DmtFilterRepository;
import com.dmt.backend.metadata.procedure.repository.DmtProcedureRepository;
import com.dmt.backend.metadata.screen.dto.ScreenRequest;
import com.dmt.backend.metadata.screen.dto.ScreenResponse;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import com.dmt.backend.metadata.screenrole.repository.DmtScreenRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DmtScreenService {

    private final DmtScreenRepository repository;
    private final DmtColumnRepository columnRepository;
    private final DmtFilterRepository filterRepository;
    private final DmtProcedureRepository procedureRepository;
    private final DmtScreenRoleRepository screenRoleRepository;

    public ScreenResponse create(ScreenRequest request) {

        log.info("Create screen requested screenCode={}", request.screenCode());

        if (repository.existsByScreenCode(request.screenCode())) {
            log.warn("Create screen failed screenCode={} reason=screen_code_exists", request.screenCode());
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Screen code already exists");
        }

        DmtScreen screen = DmtScreen.builder()
                .screenCode(request.screenCode())
                .screenName(request.screenName())
                .description(request.description())
                .selectQuery(request.selectQuery())
                .defaultPageSize(request.defaultPageSize())
                .defaultSortColumn(request.defaultSortColumn())
                .defaultSortDirection(request.defaultSortDirection())
                .active(request.active())
                .primaryKeyColumn(request.primaryKeyColumn())
                .build();

        DmtScreen saved = repository.save(screen);

        log.info("Screen created id={} screenCode={}", saved.getId(), saved.getScreenCode());

        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<ScreenResponse> getAll() {

        List<ScreenResponse> screens = repository.findAll()
                .stream()
                .map(this::map)
                .toList();

        log.info("Screens fetched count={}", screens.size());

        return screens;
    }

    @Transactional(readOnly = true)
    public ScreenResponse getById(Long id) {

        DmtScreen screen = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Get screen failed id={} reason=screen_not_found", id);
                    return new ApiException(HttpStatus.NOT_FOUND, "Screen not found");
                });

        log.info("Screen fetched id={} screenCode={}", id, screen.getScreenCode());

        return map(screen);
    }

    /**
     * Soft-delete: disables the screen rather than removing its row. This matches
     * the "Disable Screen" admin operation in the functional spec and avoids the
     * choice between a foreign-key violation and a destructive cascade across
     * columns/filters/procedures/roles/audit history that a hard delete would force.
     */
    public void delete(Long id) {

        DmtScreen screen = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Disable screen failed id={} reason=screen_not_found", id);
                    return new ApiException(HttpStatus.NOT_FOUND, "Screen not found");
                });

        screen.setActive(false);
        repository.save(screen);

        log.info("Screen disabled id={} screenCode={}", id, screen.getScreenCode());
    }

    /**
     * Permanently removes a screen and its row. Only allowed once all dependent
     * configuration (columns, filters, procedures, role mappings) has been removed
     * first, so this never relies on the database's FK behavior (cascade or
     * restrict) to decide what happens to that child data.
     */
    public void hardDelete(Long id) {

        DmtScreen screen = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Hard delete screen failed id={} reason=screen_not_found", id);
                    return new ApiException(HttpStatus.NOT_FOUND, "Screen not found");
                });

        boolean hasColumns = !columnRepository.findByScreenIdOrderByDisplayOrderAsc(id).isEmpty();
        boolean hasFilters = !filterRepository.findByScreenIdOrderByDisplayOrderAsc(id).isEmpty();
        boolean hasProcedures = !procedureRepository.findByScreenId(id).isEmpty();
        boolean hasRoles = !screenRoleRepository.findByScreenScreenCode(screen.getScreenCode()).isEmpty();

        if (hasColumns || hasFilters || hasProcedures || hasRoles) {
            log.warn(
                    "Hard delete screen rejected id={} screenCode={} hasColumns={} hasFilters={} hasProcedures={} hasRoles={}",
                    id, screen.getScreenCode(), hasColumns, hasFilters, hasProcedures, hasRoles
            );
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Cannot permanently delete a screen that still has columns, filters, procedures, or role mappings. Remove those first, or use disable instead.");
        }

        repository.deleteById(id);

        log.info("Screen permanently deleted id={} screenCode={}", id, screen.getScreenCode());
    }

    private ScreenResponse map(DmtScreen screen) {

        return new ScreenResponse(
                screen.getId(),
                screen.getScreenCode(),
                screen.getScreenName(),
                screen.getDescription(),
                screen.getSelectQuery(),
                screen.getDefaultPageSize(),
                screen.getDefaultSortColumn(),
                screen.getDefaultSortDirection(),
                screen.getActive(),
                screen.getPrimaryKeyColumn()
        );
    }
}