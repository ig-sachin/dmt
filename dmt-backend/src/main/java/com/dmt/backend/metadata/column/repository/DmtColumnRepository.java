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
}