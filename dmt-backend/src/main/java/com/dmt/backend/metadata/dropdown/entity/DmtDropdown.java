package com.dmt.backend.metadata.dropdown.entity;

import com.dmt.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dmt_dropdown")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmtDropdown extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String dropdownCode;

    @Column(nullable = false)
    private String dropdownName;

    @Lob
    @Column(nullable = false)
    private String query;

    private Boolean active;
}