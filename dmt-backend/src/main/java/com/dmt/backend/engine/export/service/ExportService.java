package com.dmt.backend.engine.export.service;

import com.dmt.backend.engine.export.dto.ExportRequest;
import com.dmt.backend.engine.query.dto.SearchRequest;
import com.dmt.backend.engine.query.dto.SearchResponse;
import com.dmt.backend.engine.query.service.QueryEngineService;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final QueryEngineService queryEngineService;
    private final DmtColumnRepository columnRepository;

    public byte[] exportCsv(
            String screenCode,
            ExportRequest request) {

        log.info(
                "CSV export requested screenCode={}",
                screenCode);

        List<DmtColumn> columns =
                columnRepository
                        .findByScreenScreenCodeOrderByDisplayOrderAsc(
                                screenCode);

        StringBuilder csv =
                new StringBuilder();

        for (DmtColumn column : columns) {
            csv.append(column.getDisplayName());
            csv.append(",");
        }

        csv.append("\n");

        int page = 0;
        int pageSize = 100;
        long totalRowsExported = 0;
        long totalRecords = 0;

        while (true) {
            SearchRequest searchRequest =
                    new SearchRequest(
                            page,
                            pageSize,
                            request.sortColumn(),
                            request.sortDirection(),
                            request.filters()
                    );

            SearchResponse response =
                    queryEngineService.search(
                            screenCode,
                            searchRequest);

            if (totalRecords == 0) {
                totalRecords = response.totalRecords();
            }

            if (response.content() == null || response.content().isEmpty()) {
                break;
            }

            for (Map<String, Object> row : response.content()) {
                for (DmtColumn column : columns) {
                    Object value = row.get(column.getColumnName());
                    csv.append(value == null ? "" : value.toString());
                    csv.append(",");
                }
                csv.append("\n");
                totalRowsExported++;
            }

            if (totalRowsExported >= totalRecords) {
                break;
            }
            page++;
        }

        log.info(
                "CSV export completed screenCode={} totalRecords={} exportedRows={}",
                screenCode,
                totalRecords,
                totalRowsExported);

        return csv.toString()
                .getBytes(StandardCharsets.UTF_8);
    }
}