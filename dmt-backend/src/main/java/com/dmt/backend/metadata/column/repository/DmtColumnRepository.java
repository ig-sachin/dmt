package com.dmt.backend.metadata.column.repository;

import com.dmt.backend.metadata.column.entity.DmtColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DmtColumnRepository
        extends JpaRepository<DmtColumn, Long> {

    List<DmtColumn> findByScreenIdOrderByDisplayOrderAsc(Long screenId);

    List<DmtColumn> findByScreenScreenCodeOrderByDisplayOrderAsc(
            String screenCode);

    Optional<DmtColumn> findByScreenScreenCodeAndColumnName(
            String screenCode,
            String columnName
    );

    /**
     * Screen codes that have at least one column wired to the given dropdown.
     * Used to authorize dropdown-option requests against the screens that actually
     * expose that dropdown, since a dropdown has no owning screen of its own.
     */
    List<DmtColumn> findByDropdownCode(String dropdownCode);
}