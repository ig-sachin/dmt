package com.dmt.backend.metadata.screen.repository;

import com.dmt.backend.metadata.screen.entity.DmtScreen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DmtScreenRepository
        extends JpaRepository<DmtScreen, Long> {

    Optional<DmtScreen> findByScreenCode(String screenCode);

    boolean existsByScreenCode(String screenCode);
}