package com.dmt.backend.metadata.filter.entity;

import com.dmt.backend.common.entity.BaseEntity;
import com.dmt.backend.metadata.screen.entity.DmtScreen;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dmt_filter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmtFilter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filterName;

    private String columnName;

    @Enumerated(EnumType.STRING)
    private FilterType filterType;

    private Boolean required;

    private String defaultValue;

    private Integer displayOrder;

    private String fromColumn;

    private String toColumn;

    @Column(columnDefinition = "TEXT")
    private String dropdownQuery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    private DmtScreen screen;
}