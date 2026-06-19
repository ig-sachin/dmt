package com.dmt.backend.engine.query.service;

import com.dmt.backend.engine.query.builder.FilterBuilder;
import com.dmt.backend.engine.query.builder.SortBuilder;
import com.dmt.backend.engine.query.dto.SearchRequest;
import com.dmt.backend.engine.query.dto.SearchResponse;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.filter.entity.DmtFilter;
import com.dmt.backend.metadata.filter.repository.DmtFilterRepository;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryEngineService {

    private final DmtScreenRepository screenRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DmtColumnRepository columnRepository;
    private final SortBuilder sortBuilder;
    private final FilterBuilder filterBuilder;

    public SearchResponse search(
            String screenCode,
            SearchRequest request) {

        DmtScreen screen =
                screenRepository
                        .findByScreenCode(screenCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Screen not found"));

        String sortColumn =
                sortBuilder.validateSortColumn(
                        screenCode,
                        request.sortColumn());

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
                        + " LIMIT "
                        + request.size()
                        + " OFFSET "
                        + (request.page() * request.size());

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

        int totalPages =
                (int) Math.ceil(
                        (double) totalRecords
                                / request.size());

        return new SearchResponse(
                content,
                totalRecords,
                totalPages,
                request.page(),
                request.size()
        );
    }



    private void validateFilterColumns(
            String screenCode,
            Map<String, Object> filters) {

        if (filters == null) {
            return;
        }

        filters.keySet()
                .forEach(column -> {

                    columnRepository
                            .findByScreenScreenCodeAndColumnName(
                                    screenCode,
                                    column)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Invalid filter column: "
                                                    + column));
                });
    }




}