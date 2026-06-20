package com.dmt.backend.audit.service;

import com.dmt.backend.audit.dto.AuditResponse;
import com.dmt.backend.audit.entity.AuditOperation;
import com.dmt.backend.audit.entity.DmtAuditLog;
import com.dmt.backend.audit.repository.DmtAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final DmtAuditLogRepository repository;

    public void log(

            String screenCode,

            String recordId,

            AuditOperation operation,

            String columnName,

            String oldValue,

            String newValue) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username =
                authentication != null
                        ? authentication.getName()
                        : "SYSTEM";

        DmtAuditLog audit =
                DmtAuditLog.builder()
                        .screenCode(screenCode)
                        .recordId(recordId)
                        .operation(operation)
                        .columnName(columnName)
                        .oldValue(oldValue)
                        .newValue(newValue)
                        .changedBy(username)
                        .build();

        repository.save(audit);

        log.info(
                "Audit logged Screen={} Record={} Operation={}",
                screenCode,
                recordId,
                operation);
    }

    public List<AuditResponse> getByScreen(
            String screenCode) {

        return repository
                .findByScreenCodeOrderByCreatedAtDesc(
                        screenCode)
                .stream()
                .map(this::map)
                .toList();
    }

    public List<AuditResponse> getByRecord(
            String screenCode,
            String recordId) {

        return repository
                .findByScreenCodeAndRecordIdOrderByCreatedAtDesc(
                        screenCode,
                        recordId)
                .stream()
                .map(this::map)
                .toList();
    }

    private AuditResponse map(
            DmtAuditLog audit) {

        return new AuditResponse(

                audit.getId(),

                audit.getScreenCode(),

                audit.getRecordId(),

                audit.getOperation(),

                audit.getColumnName(),

                audit.getOldValue(),

                audit.getNewValue(),

                audit.getChangedBy(),

                audit.getCreatedAt()
        );
    }
}