package com.dmt.backend.metadata.validation.service;

import com.dmt.backend.common.exception.ApiException;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.validation.dto.ValidationRequest;
import com.dmt.backend.metadata.validation.dto.ValidationResponse;
import com.dmt.backend.metadata.validation.entity.DmtValidation;
import com.dmt.backend.metadata.validation.repository.DmtValidationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final DmtColumnRepository columnRepository;
    private final DmtValidationRepository repository;

    public ValidationResponse create(
            ValidationRequest request) {

        log.info(
                "Creating validation. ColumnId={}, Type={}",
                request.columnId(),
                request.validationType());

        DmtColumn column =
                columnRepository
                        .findById(request.columnId())
                        .orElseThrow(() ->
                                new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Column not found"));

        DmtValidation validation =
                DmtValidation.builder()
                        .column(column)
                        .validationType(
                                request.validationType())
                        .validationValue(
                                request.validationValue())
                        .errorMessage(
                                request.errorMessage())
                        .active(
                                request.active())
                        .build();

        validation = repository.save(validation);

        log.info(
                "Validation created successfully. ValidationId={}",
                validation.getId());

        return map(validation);
    }

    @Transactional(readOnly = true)
    public List<ValidationResponse>
    getByColumn(Long columnId) {

        log.info(
                "Fetching validations for ColumnId={}",
                columnId);

        return repository
                .findByColumnIdAndActiveTrue(
                        columnId)
                .stream()
                .map(this::map)
                .toList();
    }

    public void delete(Long id) {

        log.info(
                "Deleting validation. ValidationId={}",
                id);

        if (!repository.existsById(id)) {

            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Validation not found");
        }

        repository.deleteById(id);

        log.info(
                "Validation deleted successfully. ValidationId={}",
                id);
    }

    private ValidationResponse map(
            DmtValidation validation) {

        return new ValidationResponse(
                validation.getId(),
                validation.getColumn().getId(),
                validation.getValidationType(),
                validation.getValidationValue(),
                validation.getErrorMessage(),
                validation.getActive()
        );
    }
}
