package com.dmt.backend.engine.crud.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.validation.entity.DmtValidation;
import com.dmt.backend.metadata.validation.entity.ValidationType;
import com.dmt.backend.metadata.validation.repository.DmtValidationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Re-checks DmtValidation rules (REQUIRED, MIN_LENGTH, MAX_LENGTH, REGEX, MIN_VALUE,
 * MAX_VALUE) against a submitted insert/update payload, server-side.
 *
 * Without this, these rules only ever reached the frontend via FormEngineService and
 * shaped the client form - any request sent directly to the API (a different
 * frontend, a bug, curl, a compromised client) skipped them entirely and the data
 * would only be rejected if the stored procedure happened to separately enforce it.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FieldValidationService {

    private final DmtColumnRepository columnRepository;
    private final DmtValidationRepository validationRepository;

    public void validate(String screenCode, Map<String, Object> values) {

        if (values == null) {
            return;
        }

        List<DmtColumn> columns =
                columnRepository.findByScreenScreenCodeOrderByDisplayOrderAsc(screenCode);

        for (DmtColumn column : columns) {

            Object value = values.get(column.getColumnName());

            List<DmtValidation> rules =
                    validationRepository.findByColumnIdAndActiveTrue(column.getId());

            for (DmtValidation rule : rules) {
                applyRule(screenCode, column, rule, value);
            }
        }
    }

    private void applyRule(
            String screenCode,
            DmtColumn column,
            DmtValidation rule,
            Object value) {

        ValidationType type = rule.getValidationType();

        // REQUIRED is the only rule that fires on an absent/blank value; every other
        // rule is skipped when the value isn't present, since "optional but must
        // match this pattern if supplied" is the standard interpretation.
        boolean isBlank = value == null || (value instanceof String s && s.isBlank());

        if (type == ValidationType.REQUIRED) {
            if (isBlank) {
                fail(screenCode, column, rule);
            }
            return;
        }

        if (isBlank) {
            return;
        }

        String stringValue = String.valueOf(value);

        switch (type) {

            case MIN_LENGTH -> {
                int min = parseIntRule(rule);
                if (stringValue.length() < min) {
                    fail(screenCode, column, rule);
                }
            }

            case MAX_LENGTH -> {
                int max = parseIntRule(rule);
                if (stringValue.length() > max) {
                    fail(screenCode, column, rule);
                }
            }

            case REGEX -> {
                try {
                    if (!Pattern.matches(rule.getValidationValue(), stringValue)) {
                        fail(screenCode, column, rule);
                    }
                } catch (PatternSyntaxException patternSyntaxException) {
                    log.error(
                            "Invalid REGEX validation configured screenCode={} column={} pattern={}",
                            screenCode,
                            column.getColumnName(),
                            rule.getValidationValue()
                    );
                    // A misconfigured pattern is an admin-side error, not the
                    // caller's fault - fail safe by rejecting rather than silently
                    // skipping the rule.
                    throw new ApiException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Validation rule misconfigured for column: " + column.getColumnName());
                }
            }

            case MIN_VALUE -> {
                BigDecimal min = parseDecimalRule(rule);
                BigDecimal actual = parseDecimalValue(screenCode, column, stringValue);
                if (actual.compareTo(min) < 0) {
                    fail(screenCode, column, rule);
                }
            }

            case MAX_VALUE -> {
                BigDecimal max = parseDecimalRule(rule);
                BigDecimal actual = parseDecimalValue(screenCode, column, stringValue);
                if (actual.compareTo(max) > 0) {
                    fail(screenCode, column, rule);
                }
            }

            case REQUIRED -> {
                // handled above
            }
        }
    }

    private int parseIntRule(DmtValidation rule) {
        try {
            return Integer.parseInt(rule.getValidationValue());
        } catch (NumberFormatException e) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Validation rule misconfigured: expected a numeric value");
        }
    }

    private BigDecimal parseDecimalRule(DmtValidation rule) {
        try {
            return new BigDecimal(rule.getValidationValue());
        } catch (NumberFormatException e) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Validation rule misconfigured: expected a numeric value");
        }
    }

    private BigDecimal parseDecimalValue(String screenCode, DmtColumn column, String stringValue) {
        try {
            return new BigDecimal(stringValue);
        } catch (NumberFormatException e) {
            log.warn(
                    "Numeric validation failed screenCode={} column={} value={} reason=not_a_number",
                    screenCode,
                    column.getColumnName(),
                    stringValue
            );
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    column.getDisplayName() + " must be a number");
        }
    }

    private void fail(String screenCode, DmtColumn column, DmtValidation rule) {

        String message = rule.getErrorMessage() != null && !rule.getErrorMessage().isBlank()
                ? rule.getErrorMessage()
                : defaultMessage(column, rule);

        log.warn(
                "Field validation failed screenCode={} column={} validationType={} message={}",
                screenCode,
                column.getColumnName(),
                rule.getValidationType(),
                message
        );

        throw new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    private String defaultMessage(DmtColumn column, DmtValidation rule) {

        String label = column.getDisplayName() != null ? column.getDisplayName() : column.getColumnName();

        return switch (rule.getValidationType()) {
            case REQUIRED -> label + " is required";
            case MIN_LENGTH -> label + " must be at least " + rule.getValidationValue() + " characters";
            case MAX_LENGTH -> label + " must be at most " + rule.getValidationValue() + " characters";
            case REGEX -> label + " is not in a valid format";
            case MIN_VALUE -> label + " must be at least " + rule.getValidationValue();
            case MAX_VALUE -> label + " must be at most " + rule.getValidationValue();
        };
    }
}