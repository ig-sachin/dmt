package com.dmt.backend.metadata.filter.service;

import com.dmt.backend.metadata.filter.dto.FilterRequest;
import com.dmt.backend.metadata.filter.dto.FilterResponse;
import com.dmt.backend.metadata.filter.entity.DmtFilter;
import com.dmt.backend.metadata.filter.repository.DmtFilterRepository;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DmtFilterService {

    private final DmtFilterRepository filterRepository;
    private final DmtScreenRepository screenRepository;

    public FilterResponse create(FilterRequest request) {

        DmtScreen screen = screenRepository.findById(request.screenId())
                .orElseThrow(() ->
                        new RuntimeException("Screen not found"));

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

        return map(filterRepository.save(filter));
    }

    public List<FilterResponse> getByScreen(Long screenId) {

        return filterRepository
                .findByScreenIdOrderByDisplayOrderAsc(screenId)
                .stream()
                .map(this::map)
                .toList();
    }

    public void delete(Long id) {

        filterRepository.deleteById(id);
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