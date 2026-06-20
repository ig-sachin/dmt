package com.dmt.backend.metadata.dropdownparam.repository;

import com.dmt.backend.metadata.dropdownparam.entity.DmtDropdownParam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DmtDropdownParamRepository
        extends JpaRepository<DmtDropdownParam, Long> {

    List<DmtDropdownParam>
    findByDropdownIdOrderByParameterOrderAsc(
            Long dropdownId);
}