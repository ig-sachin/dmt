package com.dmt.backend.metadata.dropdownparam.entity;

import com.dmt.backend.common.entity.BaseEntity;
import com.dmt.backend.metadata.dropdown.entity.DmtDropdown;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dmt_dropdown_param")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmtDropdownParam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String parameterName;

    private String requestField;

    private Boolean required;

    private Integer parameterOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dropdown_id")
    private DmtDropdown dropdown;
}