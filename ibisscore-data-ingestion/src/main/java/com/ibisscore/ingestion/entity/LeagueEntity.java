package com.ibisscore.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leagues")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LeagueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer apiId;

    @Column(nullable = false)
    private String name;

    private String country;
    private Integer season;
    private String logoUrl;

    @Builder.Default
    private boolean isActive = true;
}
