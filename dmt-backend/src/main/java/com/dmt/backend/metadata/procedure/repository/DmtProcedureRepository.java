package com.dmt.backend.metadata.procedure.repository;

import com.dmt.backend.metadata.procedure.entity.DmtProcedure;
import com.dmt.backend.metadata.procedure.entity.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DmtProcedureRepository
        extends JpaRepository<DmtProcedure, Long> {

    List<DmtProcedure> findByScreenId(Long screenId);

    Optional<DmtProcedure>
    findByScreenScreenCodeAndOperationTypeAndActiveTrue(
            String screenCode,
            OperationType operationType
    );
}