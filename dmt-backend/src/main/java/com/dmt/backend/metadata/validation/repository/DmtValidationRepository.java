package com.dmt.backend.metadata.validation.repository;

import com.dmt.backend.metadata.validation.entity.DmtValidation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DmtValidationRepository
        extends JpaRepository<DmtValidation, Long> {

    List<DmtValidation>
    findByColumnIdAndActiveTrue(Long columnId);
}