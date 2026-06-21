package com.dmt.backend.metadata.screenrole.entity;

import com.dmt.backend.common.entity.BaseEntity;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dmt_screen_role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmtScreenRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roleName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    private DmtScreen screen;

    /**
     * Operation-level permissions. Default to true so existing rows created before
     * this column existed (and any row that omits these fields) keep their current
     * behavior of full access once screen-level access is granted.
     */
    @Builder.Default
    private Boolean canView = true;

    @Builder.Default
    private Boolean canInsert = true;

    @Builder.Default
    private Boolean canUpdate = true;

    @Builder.Default
    private Boolean canDelete = true;

    public boolean permits(PermissionType permissionType) {
        return switch (permissionType) {
            case VIEW -> Boolean.TRUE.equals(canView);
            case INSERT -> Boolean.TRUE.equals(canInsert);
            case UPDATE -> Boolean.TRUE.equals(canUpdate);
            case DELETE -> Boolean.TRUE.equals(canDelete);
        };
    }
}