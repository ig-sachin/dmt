package com.dmt.backend.metadata.procedure.entity;

import com.dmt.backend.common.entity.BaseEntity;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dmt_procedure")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmtProcedure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    private String procedureName;

    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    private DmtScreen screen;
}