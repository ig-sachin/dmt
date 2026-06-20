package com.dmt.backend.engine.dropdown.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.engine.dropdown.dto.DropdownOptionResponse;
import com.dmt.backend.metadata.dropdown.entity.DmtDropdown;
import com.dmt.backend.metadata.dropdown.repository.DmtDropdownRepository;
import com.dmt.backend.metadata.dropdownparam.entity.DmtDropdownParam;
import com.dmt.backend.metadata.dropdownparam.repository.DmtDropdownParamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DropdownEngineService {

    private final DmtDropdownRepository dropdownRepository;
    private final DmtDropdownParamRepository paramRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<DropdownOptionResponse> getOptions(
            String dropdownCode,
            Map<String, String> requestParams) {

        DmtDropdown dropdown =
                dropdownRepository
                        .findByDropdownCodeAndActiveTrue(
                                dropdownCode)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Dropdown options failed dropdownCode={} reason=dropdown_not_found",
                                    dropdownCode
                            );

                            return new ApiException(
                                    HttpStatus.NOT_FOUND,
                                    "Dropdown not found");
                        });

        List<DmtDropdownParam> params =
                paramRepository
                        .findByDropdownIdOrderByParameterOrderAsc(
                                dropdown.getId());

        MapSqlParameterSource sqlParams =
                new MapSqlParameterSource();

        for (DmtDropdownParam param : params) {

            String value =
                    requestParams.get(
                            param.getRequestField());

            if (Boolean.TRUE.equals(
                    param.getRequired())
                    && value == null) {

                log.warn(
                        "Dropdown options failed dropdownCode={} missingParameter={}",
                        dropdownCode,
                        param.getRequestField()
                );

                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Missing parameter: "
                                + param.getRequestField());
            }

            sqlParams.addValue(
                    param.getParameterName(),
                    value);
        }

        log.info(
                "Executing dropdown dropdownCode={} paramCount={}",
                dropdownCode,
                params.size());

        List<Map<String, Object>> rows =
                jdbcTemplate.queryForList(
                        dropdown.getQuery(),
                        sqlParams);

        log.info(
                "Dropdown resolved dropdownCode={} optionCount={}",
                dropdownCode,
                rows.size()
        );

        return rows.stream()
                .map(row ->
                        new DropdownOptionResponse(
                                String.valueOf(
                                        row.get("VALUE")),
                                String.valueOf(
                                        row.get("LABEL"))
                        ))
                .toList();
    }
}
