package com.dmt.backend.metadata.filter.repository;

import com.dmt.backend.metadata.filter.entity.DmtFilter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DmtFilterRepository
        extends JpaRepository<DmtFilter, Long> {

    List<DmtFilter> findByScreenIdOrderByDisplayOrderAsc(Long screenId);

    List<DmtFilter> findByScreenScreenCodeOrderByDisplayOrderAsc(String screenCode);
}