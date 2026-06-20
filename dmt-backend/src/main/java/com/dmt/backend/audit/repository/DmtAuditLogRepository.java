package com.dmt.backend.audit.repository;

import com.dmt.backend.audit.entity.DmtAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DmtAuditLogRepository
        extends JpaRepository<DmtAuditLog, Long> {

    List<DmtAuditLog>
    findByScreenCodeOrderByCreatedAtDesc(
            String screenCode);

    List<DmtAuditLog>
    findByScreenCodeAndRecordIdOrderByCreatedAtDesc(
            String screenCode,
            String recordId);
}