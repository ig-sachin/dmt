package com.dmt.backend.metadata.screenrole.entity;

/**
 * Operation-level permission checked by {@link com.dmt.backend.security.ScreenAuthorizationService}.
 * Distinct from {@link com.dmt.backend.metadata.procedure.entity.OperationType}, which only
 * covers write operations that map to a stored procedure - VIEW has no procedure mapping
 * but still needs to be authorized.
 */
public enum PermissionType {
    VIEW,
    INSERT,
    UPDATE,
    DELETE
}