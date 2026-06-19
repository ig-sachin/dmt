package com.dmt.backend.metadata.procedureparam.entity;

import com.dmt.backend.common.entity.BaseEntity;
import com.dmt.backend.metadata.procedure.entity.DmtProcedure;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DMT_PROCEDURE_PARAM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmtProcedureParam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String parameterName;

    private Integer parameterOrder;

    private String columnName;

    private String defaultValue;

    private Boolean required;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id")
    private DmtProcedure procedure;
}