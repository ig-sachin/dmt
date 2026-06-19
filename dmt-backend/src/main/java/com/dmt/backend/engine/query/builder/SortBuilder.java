package com.dmt.backend.engine.query.builder;

import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
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
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid sort column: "
                                        + sortColumn));
    }

    public String validateSortDirection(
            String direction) {

        if (direction == null) {
            return "ASC";
        }

        if (!direction.equalsIgnoreCase("ASC")
                && !direction.equalsIgnoreCase("DESC")) {

            throw new RuntimeException(
                    "Invalid sort direction");
        }

        return direction.toUpperCase();
    }
}
