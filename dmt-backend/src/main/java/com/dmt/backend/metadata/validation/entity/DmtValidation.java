package com.dmt.backend.metadata.validation.entity;

import com.dmt.backend.common.entity.BaseEntity;
import com.dmt.backend.metadata.column.entity.DmtColumn;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dmt_validation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmtValidation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ValidationType validationType;

    private String validationValue;

    private String errorMessage;

    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "column_id")
    private DmtColumn column;
}