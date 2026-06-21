package com.dmt.backend.metadata.screenrole.dto;

public record ScreenRoleResponse(

        Long id,

        Long screenId,

        String screenCode,

        String roleName,

        Boolean canView,

        Boolean canInsert,

        Boolean canUpdate,

        Boolean canDelete

) {
}