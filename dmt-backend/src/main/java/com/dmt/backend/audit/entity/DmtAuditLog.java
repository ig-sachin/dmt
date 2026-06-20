package com.dmt.backend.audit.entity;

import com.dmt.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dmt_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmtAuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String screenCode;

    private String recordId;

    @Enumerated(EnumType.STRING)
    private AuditOperation operation;

    private String columnName;

    @Lob
    private String oldValue;

    @Lob
    private String newValue;

    private String changedBy;
}