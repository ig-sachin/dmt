package com.dmt.backend.role.repository;

import com.dmt.backend.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository
        extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);
}