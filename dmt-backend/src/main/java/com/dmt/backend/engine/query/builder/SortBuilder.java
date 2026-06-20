package com.dmt.backend.engine.query.builder;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class SortBuilder {

    private final DmtColumnRepository columnRepository;

    public String validateSortColumn(
            String screenCode,
            String sortColumn) {

        return columnRepository
                .findByScreenScreenCodeAndColumnName(
                        screenCode,
                        sortColumn)
                .map(DmtColumn::getColumnName)
                .orElseThrow(() -> {
                    log.warn(
                            "Invalid sort column screenCode={} sortColumn={}",
                            screenCode,
                            sortColumn
                    );

                    return new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "Invalid sort column: "
                                    + sortColumn);
                });
    }

    public String validateSortDirection(
            String direction) {

        if (direction == null) {
            return "ASC";
        }

        if (!direction.equalsIgnoreCase("ASC")
                && !direction.equalsIgnoreCase("DESC")) {

            log.warn("Invalid sort direction direction={}", direction);
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort direction");
        }

        return direction.toUpperCase();
    }
}
