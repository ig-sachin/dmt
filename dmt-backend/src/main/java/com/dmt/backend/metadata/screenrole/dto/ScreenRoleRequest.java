package com.dmt.backend.metadata.screenrole.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScreenRoleRequest(

        @NotNull(message = "Screen id is required")
        Long screenId,

        @NotBlank(message = "Role name is required")
        String roleName,

        Boolean canView,

        Boolean canInsert,

        Boolean canUpdate,

        Boolean canDelete

) {
}