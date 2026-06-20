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
}