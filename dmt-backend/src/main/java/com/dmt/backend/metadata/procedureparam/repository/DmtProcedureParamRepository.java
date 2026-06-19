package com.dmt.backend.metadata.procedureparam.repository;

import com.dmt.backend.metadata.procedureparam.entity.DmtProcedureParam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DmtProcedureParamRepository
        extends JpaRepository<DmtProcedureParam, Long> {

    List<DmtProcedureParam>
    findByProcedureIdOrderByParameterOrderAsc(
            Long procedureId);
}