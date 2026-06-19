package com.dmt.backend.engine.query.builder;

import com.dmt.backend.metadata.filter.entity.DmtFilter;
import com.dmt.backend.metadata.filter.repository.DmtFilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class FilterBuilder {

    private final DmtFilterRepository filterRepository;

    public String buildWhereClause(
            String screenCode,
            Map<String, Object> filters,
            MapSqlParameterSource params) {

        if (filters == null || filters.isEmpty()) {
            return "";
        }

        Map<String, DmtFilter> filterMap =
                getFilterMap(screenCode);

        StringBuilder where =
                new StringBuilder(" WHERE 1=1 ");

        filters.forEach((column, value) -> {

            DmtFilter filter =
                    filterMap.get(column);

            if (filter == null) {
                throw new RuntimeException(
                        "Invalid filter: " + column);
            }

            switch (filter.getFilterType()) {

                case TEXT -> {

                    where.append(" AND ")
                            .append(column)
                            .append(" ILIKE :")
                            .append(column);

                    params.addValue(
                            column,
                            "%" + value + "%"
                    );
                }

                case DROPDOWN -> {

                    if (value instanceof java.util.Collection<?>) {

                        where.append(" AND ")
                                .append(column)
                                .append(" IN (:")
                                .append(column)
                                .append(")");

                    } else {

                        where.append(" AND ")
                                .append(column)
                                .append(" = :")
                                .append(column);
                    }

                    params.addValue(column, value);
                }

                case MULTI_SELECT -> {

                    where.append(" AND ")
                            .append(column)
                            .append(" IN (:")
                            .append(column)
                            .append(")");

                    params.addValue(
                            column,
                            value
                    );
                }

                default ->
                        throw new RuntimeException(
                                "Unsupported filter type");
            }
        });

        return where.toString();
    }

    private Map<String, DmtFilter> getFilterMap(String screenCode) {

        return filterRepository
                .findByScreenScreenCodeOrderByDisplayOrderAsc(screenCode)
                .stream()
                .collect(Collectors.toMap(
                        DmtFilter::getColumnName,
                        filter -> filter
                ));
    }
}
