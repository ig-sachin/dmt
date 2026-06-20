package com.dmt.backend.metadata.column.service;

import com.dmt.backend.metadata.column.dto.ColumnRequest;
import com.dmt.backend.metadata.column.dto.ColumnResponse;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import com.dmt.backend.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DmtColumnService {

    private final DmtColumnRepository columnRepository;
    private final DmtScreenRepository screenRepository;

    public ColumnResponse create(ColumnRequest request) {

        log.info(
                "Create column requested screenId={} columnName={}",
                request.screenId(),
                request.columnName()
        );

        DmtScreen screen = screenRepository.findById(request.screenId())
                .orElseThrow(() -> {
                    log.warn("Create column failed screenId={} reason=screen_not_found", request.screenId());
                    return new ApiException(HttpStatus.NOT_FOUND, "Screen not found");
                });
        DmtColumn column = DmtColumn.builder()
                .screen(screen)
                .columnName(request.columnName())
                .displayName(request.displayName())
                .dataType(request.dataType())
                .fieldType(request.fieldType())
                .visible(request.visible())
                .editable(request.editable())
                .mandatory(request.mandatory())
                .defaultValue(request.defaultValue())
                .displayOrder(request.displayOrder())
                .width(request.width())
                .alignment(request.alignment())
                .formatMask(request.formatMask())
                .placeholder(request.placeHolder())
                .maxLength(request.maxLength())
                .dropdownCode(request.dropdownCode())
                .build();

        DmtColumn saved = columnRepository.save(column);

        log.info(
                "Column created id={} screenId={} columnName={}",
                saved.getId(),
                request.screenId(),
                request.columnName()
        );

        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<ColumnResponse> getByScreen(Long screenId) {

        List<ColumnResponse> columns = columnRepository
                .findByScreenIdOrderByDisplayOrderAsc(screenId)
                .stream()
                .map(this::map)
                .toList();

        log.info("Columns fetched screenId={} count={}", screenId, columns.size());

        return columns;
    }

    public void delete(Long id) {

        columnRepository.deleteById(id);

        log.info("Column deleted id={}", id);
    }

    private ColumnResponse map(DmtColumn column) {

        return new ColumnResponse(
                column.getId(),
                column.getScreen().getId(),
                column.getColumnName(),
                column.getDisplayName(),
                column.getDataType(),
                column.getFieldType(),
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
                column.getDropdownCode()
        );
    }
}
