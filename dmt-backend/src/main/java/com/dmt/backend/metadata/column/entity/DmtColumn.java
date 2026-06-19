package com.dmt.backend.metadata.column.entity;

import com.dmt.backend.common.entity.BaseEntity;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dmt_column")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmtColumn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String columnName;

    private String displayName;

    private String dataType;

    @Enumerated(EnumType.STRING)
    private FieldType fieldType;

    private Boolean visible;

    private Boolean editable;

    private Boolean mandatory;

    private String defaultValue;

    private Integer displayOrder;

    private Integer width;

    private String alignment;

    private String formatMask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    private DmtScreen screen;
}