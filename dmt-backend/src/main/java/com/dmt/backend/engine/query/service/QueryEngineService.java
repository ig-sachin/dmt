package com.dmt.backend.engine.query.service;

import com.dmt.backend.engine.query.builder.FilterBuilder;
import com.dmt.backend.engine.query.builder.SortBuilder;
import com.dmt.backend.engine.query.dto.SearchRequest;
import com.dmt.backend.engine.query.dto.SearchResponse;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryEngineService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int FALLBACK_PAGE_SIZE = 20;

    private final DmtScreenRepository screenRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DmtColumnRepository columnRepository;
    private final SortBuilder sortBuilder;
    private final FilterBuilder filterBuilder;

    public SearchResponse search(
            String screenCode,
            SearchRequest request) {

        if (request == null) {
            log.warn("Search request body is empty screenCode={}", screenCode);
            request = new SearchRequest(null, null, null, null, null);
        }

        log.info(
                "Search requested screenCode={} page={} size={} sortColumn={} sortDirection={} filterKeys={}",
                screenCode,
                request.page(),
                request.size(),
                request.sortColumn(),
                request.sortDirection(),
                request.filters() == null ? null : request.filters().keySet()
        );

        DmtScreen screen =
                screenRepository
                        .findByScreenCode(screenCode)
                        .orElseThrow(() -> {
                            log.warn("Search failed screenCode={} reason=screen_not_found", screenCode);
                            return new RuntimeException("Screen not found");
                        });

        int page = resolvePage(request);
        int size = resolveSize(request, screen);
        String requestedSortColumn = resolveSortColumn(request, screen);

        String sortColumn =
                sortBuilder.validateSortColumn(
                        screenCode,
                        requestedSortColumn);

        String sortDirection =
                sortBuilder.validateSortDirection(
                        request.sortDirection());

        validateFilterColumns(
                screenCode,
                request.filters());

        MapSqlParameterSource params =
                new MapSqlParameterSource();

        String whereClause =
                filterBuilder.buildWhereClause(
                        screenCode,
                        request.filters(),
                        params);

        String baseQuery =
                "(" + screen.getSelectQuery() + ") data";

        String dataQuery =
                "SELECT * FROM "
                        + baseQuery
                        + whereClause
                        + " ORDER BY "
                        + sortColumn
                        + " "
                        + sortDirection
                        + " OFFSET "
                        + (page * size)
                        + " ROWS FETCH NEXT "
                        + size
                        + " ROWS ONLY";

        String countQuery =
                "SELECT COUNT(*) FROM "
                        + baseQuery
                        + whereClause;

        Long totalRecords =
                jdbcTemplate.queryForObject(
                        countQuery,
                        params,
                        Long.class);

        List<Map<String, Object>> content =
                jdbcTemplate.queryForList(
                        dataQuery,
                        params);

        long safeTotalRecords =
                totalRecords == null ? 0 : totalRecords;

        int totalPages =
                (int) Math.ceil(
                        (double) safeTotalRecords / size);

        log.info(
                "Search completed screenCode={} page={} size={} totalRecords={} totalPages={} returnedRecords={}",
                screenCode,
                page,
                size,
                safeTotalRecords,
                totalPages,
                content.size()
        );

        return new SearchResponse(
                content,
                safeTotalRecords,
                totalPages,
                page,
                size
        );
    }

    private int resolvePage(SearchRequest request) {

        if (request.page() == null || request.page() < 0) {
            return 0;
        }

        return request.page();
    }

    private int resolveSize(
            SearchRequest request,
            DmtScreen screen) {

        int size;

        if (request.size() == null || request.size() <= 0) {
            size = screen.getDefaultPageSize() == null
                    ? FALLBACK_PAGE_SIZE
                    : screen.getDefaultPageSize();
        } else {
            size = request.size();
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String resolveSortColumn(
            SearchRequest request,
            DmtScreen screen) {

        if (request.sortColumn() == null
                || request.sortColumn().isBlank()) {

            return screen.getDefaultSortColumn();
        }

        return request.sortColumn();
    }

    private void validateFilterColumns(
            String screenCode,
            Map<String, Object> filters) {

        if (filters == null || filters.isEmpty()) {
            return;
        }

        filters.keySet()
                .forEach(column -> columnRepository
                        .findByScreenScreenCodeAndColumnName(
                                screenCode,
                                column)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Invalid filter column screenCode={} column={}",
                                    screenCode,
                                    column
                            );

                            return new RuntimeException(
                                    "Invalid filter column: "
                                            + column);
                        }));
    }
}
