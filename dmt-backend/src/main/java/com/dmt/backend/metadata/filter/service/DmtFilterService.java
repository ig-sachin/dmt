package com.dmt.backend.metadata.filter.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.metadata.filter.dto.FilterRequest;
import com.dmt.backend.metadata.filter.dto.FilterResponse;
import com.dmt.backend.metadata.filter.entity.DmtFilter;
import com.dmt.backend.metadata.filter.repository.DmtFilterRepository;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DmtFilterService {

    private final DmtFilterRepository filterRepository;
    private final DmtScreenRepository screenRepository;
    private final DmtColumnRepository columnRepository;

    public FilterResponse create(FilterRequest request) {

        log.info(
                "Create filter requested screenId={} filterName={} columnName={}",
                request.screenId(),
                request.filterName(),
                request.columnName()
        );

        DmtScreen screen = screenRepository.findById(request.screenId())
                .orElseThrow(() -> {
                    log.warn("Create filter failed screenId={} reason=screen_not_found", request.screenId());
                    return new ApiException(HttpStatus.NOT_FOUND, "Screen not found");
                });

        boolean columnExists =
                columnRepository
                        .findByScreenScreenCodeAndColumnName(
                                screen.getScreenCode(),
                                request.columnName())
                        .isPresent();

        if (!columnExists) {
            log.warn(
                    "Create filter failed screenId={} columnName={} reason=column_not_found",
                    request.screenId(),
                    request.columnName()
            );
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Column '" + request.columnName() + "' is not configured for this screen");
        }

        DmtFilter filter = DmtFilter.builder()
                .screen(screen)
                .filterName(request.filterName())
                .columnName(request.columnName())
                .filterType(request.filterType())
                .required(request.required())
                .defaultValue(request.defaultValue())
                .displayOrder(request.displayOrder())
                .dropdownQuery(request.dropdownQuery())
                .build();

        DmtFilter saved = filterRepository.save(filter);

        log.info(
                "Filter created id={} screenId={} columnName={}",
                saved.getId(),
                request.screenId(),
                request.columnName()
        );

        return map(saved);
    }

    public List<FilterResponse> getByScreen(Long screenId) {

        List<FilterResponse> filters = filterRepository
                .findByScreenIdOrderByDisplayOrderAsc(screenId)
                .stream()
                .map(this::map)
                .toList();

        log.info("Filters fetched screenId={} count={}", screenId, filters.size());

        return filters;
    }

    public void delete(Long id) {

        if (!filterRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Filter not found");
        }

        filterRepository.deleteById(id);

        log.info("Filter deleted id={}", id);
    }

    private FilterResponse map(DmtFilter filter) {

        return new FilterResponse(
                filter.getId(),
                filter.getScreen().getId(),
                filter.getFilterName(),
                filter.getColumnName(),
                filter.getFilterType(),
                filter.getRequired(),
                filter.getDefaultValue(),
                filter.getDisplayOrder(),
                filter.getDropdownQuery()
        );
    }
}