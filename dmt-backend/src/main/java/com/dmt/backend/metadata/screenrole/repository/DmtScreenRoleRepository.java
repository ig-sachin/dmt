package com.dmt.backend.metadata.screenrole.repository;

import com.dmt.backend.metadata.screenrole.entity.DmtScreenRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DmtScreenRoleRepository
        extends JpaRepository<DmtScreenRole, Long> {

    List<DmtScreenRole> findByScreenScreenCode(String screenCode);
}