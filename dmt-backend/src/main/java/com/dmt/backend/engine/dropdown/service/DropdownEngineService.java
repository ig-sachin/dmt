package com.dmt.backend.engine.dropdown.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.engine.dropdown.dto.DropdownOptionResponse;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.dropdown.entity.DmtDropdown;
import com.dmt.backend.metadata.dropdown.repository.DmtDropdownRepository;
import com.dmt.backend.metadata.dropdownparam.entity.DmtDropdownParam;
import com.dmt.backend.metadata.dropdownparam.repository.DmtDropdownParamRepository;
import com.dmt.backend.metadata.screenrole.entity.PermissionType;
import com.dmt.backend.security.ScreenAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DropdownEngineService {

    private final DmtDropdownRepository dropdownRepository;
    private final DmtDropdownParamRepository paramRepository;
    private final DmtColumnRepository columnRepository;
    private final ScreenAuthorizationService authorizationService;
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

        authorizeAgainstReferencingScreens(dropdownCode);

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

    /**
     * A dropdown has no owning screen of its own - it can be referenced by columns on
     * one or more screens. We authorize the caller if they have VIEW access to at
     * least one screen that actually references this dropdown. If no screen
     * references it (orphaned/misconfigured dropdown), we fail closed.
     */
    private void authorizeAgainstReferencingScreens(String dropdownCode) {

        List<String> referencingScreenCodes =
                columnRepository.findByDropdownCode(dropdownCode)
                        .stream()
                        .map(DmtColumn::getScreen)
                        .filter(java.util.Objects::nonNull)
                        .map(screen -> screen.getScreenCode())
                        .distinct()
                        .toList();

        if (referencingScreenCodes.isEmpty()) {
            log.warn(
                    "Dropdown options denied dropdownCode={} reason=not_referenced_by_any_screen",
                    dropdownCode
            );
            throw new AccessDeniedException(
                    "Access denied for dropdown " + dropdownCode);
        }

        boolean authorized = false;

        for (String screenCode : referencingScreenCodes) {
            try {
                authorizationService.authorize(screenCode, PermissionType.VIEW);
                authorized = true;
                break;
            } catch (AccessDeniedException ignored) {
                // try the next referencing screen
            }
        }

        if (!authorized) {
            log.warn(
                    "Dropdown options denied dropdownCode={} reason=no_viewable_referencing_screen referencingScreens={}",
                    dropdownCode,
                    referencingScreenCodes
            );
            throw new AccessDeniedException(
                    "Access denied for dropdown " + dropdownCode);
        }
    }
}