package com.dmt.backend.engine.form.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.engine.form.dto.FormFieldResponse;
import com.dmt.backend.engine.form.dto.FormResponse;
import com.dmt.backend.engine.form.dto.FormValidationResponse;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import com.dmt.backend.metadata.screenrole.entity.PermissionType;
import com.dmt.backend.metadata.validation.entity.DmtValidation;
import com.dmt.backend.metadata.validation.repository.DmtValidationRepository;
import com.dmt.backend.security.ScreenAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FormEngineService {

    private final DmtScreenRepository screenRepository;
    private final DmtColumnRepository columnRepository;
    private final DmtValidationRepository validationRepository;
    private final ScreenAuthorizationService authorizationService;

    public FormResponse getForm(
            String screenCode) {

        authorizationService.authorize(screenCode, PermissionType.VIEW);
        log.info(
                "Loading form metadata for screen={}",
                screenCode);
        DmtScreen screen =
                screenRepository
                        .findByScreenCode(screenCode)
                        .orElseThrow(() ->
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Screen not found"));

        List<DmtColumn> columnList =
                columnRepository
                        .findByScreenScreenCodeOrderByDisplayOrderAsc(
                                screenCode);

        // Batch-load validations for all columns on this screen in one query instead
        // of one query per column (avoids N+1).
        List<Long> columnIds = columnList.stream().map(DmtColumn::getId).toList();

        Map<Long, List<DmtValidation>> validationsByColumnId =
                columnIds.isEmpty()
                        ? Map.of()
                        : validationRepository
                        .findByColumnIdInAndActiveTrue(columnIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                validation -> validation.getColumn().getId()));

        List<FormFieldResponse> fields =
                columnList.stream()
                        .map(column -> map(column, validationsByColumnId.getOrDefault(column.getId(), List.of())))
                        .toList();

        log.info(
                "Loaded {} fields for screen={}",
                fields.size(),
                screenCode);

        return new FormResponse(
                screen.getScreenCode(),
                screen.getScreenName(),
                fields
        );
    }

    private FormFieldResponse map(
            DmtColumn column,
            List<DmtValidation> validations) {

        var validationResponses =
                validations
                        .stream()
                        .map(this::mapValidation)
                        .toList();

        return new FormFieldResponse(

                column.getColumnName(),

                column.getDisplayName(),

                column.getDataType(),

                column.getFieldType().name(),

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

                column.getDropdownCode(),

                validationResponses
        );
    }

    private FormValidationResponse mapValidation(
            DmtValidation validation) {

        return new FormValidationResponse(

                validation.getValidationType().name(),

                validation.getValidationValue(),

                validation.getErrorMessage()
        );
    }
}