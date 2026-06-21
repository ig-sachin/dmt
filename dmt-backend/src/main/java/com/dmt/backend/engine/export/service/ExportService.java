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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;

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
                    csv.append(escapeCsv(value));
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

    public byte[] exportExcel(
            String screenCode,
            ExportRequest request) {

        log.info(
                "Excel export requested screenCode={}",
                screenCode);

        List<DmtColumn> columns =
                columnRepository
                        .findByScreenScreenCodeOrderByDisplayOrderAsc(
                                screenCode);

        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            Sheet sheet =
                    workbook.createSheet(screenCode);

            createHeaderRow(
                    workbook,
                    sheet,
                    columns);

            int currentRow = 1;

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
                    totalRecords =
                            response.totalRecords();
                }

                if (response.content() == null
                        || response.content().isEmpty()) {
                    break;
                }

                currentRow =
                        createDataRows(
                                sheet,
                                columns,
                                response.content(),
                                currentRow);

                totalRowsExported +=
                        response.content().size();

                if (totalRowsExported >= totalRecords) {
                    break;
                }

                page++;
            }

            autoSizeColumns(
                    sheet,
                    columns.size());

            workbook.write(outputStream);

            log.info(
                    "Excel export completed screenCode={} totalRecords={} exportedRows={}",
                    screenCode,
                    totalRecords,
                    totalRowsExported);

            return outputStream.toByteArray();

        } catch (Exception ex) {

            log.error(
                    "Excel export failed screenCode={}",
                    screenCode,
                    ex);

            throw new RuntimeException(
                    "Failed to generate Excel file",
                    ex);
        }
    }

    private void createHeaderRow(
            Workbook workbook,
            Sheet sheet,
            List<DmtColumn> columns) {

        CellStyle headerStyle =
                workbook.createCellStyle();

        Font headerFont =
                workbook.createFont();

        headerFont.setBold(true);

        headerStyle.setFont(
                headerFont);

        Row headerRow =
                sheet.createRow(0);

        int columnIndex = 0;

        for (DmtColumn column : columns) {

            Cell cell =
                    headerRow.createCell(
                            columnIndex++);

            cell.setCellValue(
                    column.getDisplayName());

            cell.setCellStyle(
                    headerStyle);
        }
    }

    private void autoSizeColumns(
            Sheet sheet,
            int totalColumns) {

        for (int i = 0;
             i < totalColumns;
             i++) {

            sheet.autoSizeColumn(i);
        }
    }

    private int createDataRows(
            Sheet sheet,
            List<DmtColumn> columns,
            List<Map<String, Object>> rows,
            int startRow) {

        int rowNumber = startRow;

        for (Map<String, Object> rowData : rows) {

            Row row =
                    sheet.createRow(rowNumber++);

            int columnIndex = 0;

            for (DmtColumn column : columns) {

                Cell cell =
                        row.createCell(columnIndex++);

                Object value =
                        rowData.get(
                                column.getColumnName());

                if (value == null) {

                    cell.setCellValue("");

                } else if (value instanceof Number number) {

                    cell.setCellValue(
                            number.doubleValue());

                } else if (value instanceof Boolean bool) {

                    cell.setCellValue(bool);

                } else {

                    cell.setCellValue(
                            value.toString());
                }
            }
        }

        return rowNumber;
    }

    private String escapeCsv(
            Object value) {

        if (value == null) {
            return "";
        }

        String text =
                value.toString();

        if (text.contains(",")
                || text.contains("\"")
                || text.contains("\n")) {

            text =
                    text.replace(
                            "\"",
                            "\"\"");

            return "\"" + text + "\"";
        }

        return text;
    }
}