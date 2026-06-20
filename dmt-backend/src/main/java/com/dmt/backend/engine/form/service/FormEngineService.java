package com.dmt.backend.engine.form.service;

import com.dmt.backend.engine.form.dto.FormFieldResponse;
import com.dmt.backend.engine.form.dto.FormResponse;
import com.dmt.backend.engine.form.dto.FormValidationResponse;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import com.dmt.backend.metadata.validation.repository.DmtValidationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FormEngineService {

    private final DmtScreenRepository screenRepository;
    private final DmtColumnRepository columnRepository;
    private final DmtValidationRepository validationRepository;

    public FormResponse getForm(
            String screenCode) {
        log.info(
                "Loading form metadata for screen={}",
                screenCode);
        DmtScreen screen =
                screenRepository
                        .findByScreenCode(screenCode)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Screen not found"));

        List<FormFieldResponse> fields =
                columnRepository
                        .findByScreenScreenCodeOrderByDisplayOrderAsc(
                                screenCode)
                        .stream()
                        .map(this::map)
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
            DmtColumn column) {

        var validations =
                validationRepository
                        .findByColumnIdAndActiveTrue(
                                column.getId())
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

                validations
        );
    }

    private FormValidationResponse mapValidation(
            com.dmt.backend.metadata.validation.entity.DmtValidation validation) {

        return new FormValidationResponse(

                validation.getValidationType().name(),

                validation.getValidationValue(),

                validation.getErrorMessage()
        );
    }
}