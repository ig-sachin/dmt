package com.dmt.backend.metadata.dropdown.repository;

import com.dmt.backend.metadata.dropdown.entity.DmtDropdown;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DmtDropdownRepository
        extends JpaRepository<DmtDropdown, Long> {

    Optional<DmtDropdown>
    findByDropdownCodeAndActiveTrue(
            String dropdownCode
    );

    boolean existsByDropdownCode(
            String dropdownCode
    );
}