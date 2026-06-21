package com.dmt.backend.metadata.metadata.service;

import com.dmt.backend.metadata.column.dto.ColumnResponse;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.filter.dto.FilterResponse;
import com.dmt.backend.metadata.filter.entity.DmtFilter;
import com.dmt.backend.metadata.filter.repository.DmtFilterRepository;
import com.dmt.backend.metadata.metadata.dto.MetadataResponse;
import com.dmt.backend.metadata.screen.dto.ScreenResponse;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import com.dmt.backend.metadata.screenrole.entity.PermissionType;
import com.dmt.backend.security.ScreenAuthorizationService;
import com.dmt.backend.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetadataService {

    private final DmtScreenRepository screenRepository;
    private final DmtColumnRepository columnRepository;
    private final DmtFilterRepository filterRepository;
    private final ScreenAuthorizationService authorizationService;

    public MetadataResponse getMetadata(
            String screenCode) {

        authorizationService.authorize(screenCode, PermissionType.VIEW);
        log.info("Metadata requested screenCode={}", screenCode);

        DmtScreen screen = screenRepository
                .findByScreenCode(screenCode)
                .orElseThrow(() -> {
                    log.warn("Metadata request failed screenCode={} reason=screen_not_found", screenCode);
                    return new ApiException(
                            HttpStatus.NOT_FOUND,
                            "Screen not found");
                });

        ScreenResponse screenResponse =
                new ScreenResponse(
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

        List<ColumnResponse> columns =
                columnRepository
                        .findByScreenScreenCodeOrderByDisplayOrderAsc(
                                screenCode)
                        .stream()
                        .map(this::mapColumn)
                        .toList();

        List<FilterResponse> filters =
                filterRepository
                        .findByScreenScreenCodeOrderByDisplayOrderAsc(
                                screenCode)
                        .stream()
                        .map(this::mapFilter)
                        .toList();

        log.info(
                "Metadata fetched screenCode={} columnCount={} filterCount={}",
                screenCode,
                columns.size(),
                filters.size()
        );

        return new MetadataResponse(
                screenResponse,
                columns,
                filters
        );
    }

    private ColumnResponse mapColumn(
            DmtColumn column) {

        return new ColumnResponse(
                column.getId(),
                column.getScreen().getId(),
                column.getColumnName(),
                column.getDisplayName(),
                column.getDataType(),
                column.getFieldType(),
                column.getVisible(),
                column.getEditable(),
                column.getMandatory(),
                column.getDefaultValue(),
                column.getDisplayOrder(),
                column.getWidth(),
                column.getAlignment(),
                column.getFormatMask(),
                column.getPlaceholder(),
                column.getMaxLength(),
                column.getDropdownCode()
        );
    }

    private FilterResponse mapFilter(
            DmtFilter filter) {

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