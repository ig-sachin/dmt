package com.dmt.backend.metadata.column.service;

import com.dmt.backend.metadata.column.dto.ColumnRequest;
import com.dmt.backend.metadata.column.dto.ColumnResponse;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import com.dmt.backend.metadata.column.repository.DmtColumnRepository;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import com.dmt.backend.metadata.screen.repository.DmtScreenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DmtColumnService {

    private final DmtColumnRepository columnRepository;
    private final DmtScreenRepository screenRepository;

    public ColumnResponse create(ColumnRequest request) {

        DmtScreen screen = screenRepository.findById(request.screenId())
                .orElseThrow(() ->
                        new RuntimeException("Screen not found"));

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
                .build();

        return map(columnRepository.save(column));
    }

    public List<ColumnResponse> getByScreen(Long screenId) {

        return columnRepository
                .findByScreenIdOrderByDisplayOrderAsc(screenId)
                .stream()
                .map(this::map)
                .toList();
    }

    public void delete(Long id) {

        columnRepository.deleteById(id);
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
                column.getFormatMask()
        );
    }
}