package com.dmt.backend.metadata.screenrole.dto;

public record ScreenRoleRequest(

        Long screenId,

        String roleName,

        Boolean canView,

        Boolean canInsert,

        Boolean canUpdate,

        Boolean canDelete

) {
}