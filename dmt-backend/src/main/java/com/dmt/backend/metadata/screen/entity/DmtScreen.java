package com.dmt.backend.metadata.screen.entity;

import com.dmt.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dmt_screen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DmtScreen extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String screenCode;

    private String screenName;

    private String description;

    @Lob
    private String selectQuery;

    private Integer defaultPageSize;

    private String defaultSortColumn;

    private String defaultSortDirection;

    private Boolean active;
}