package com.ibisscore.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teams")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer apiId;

    @Column(nullable = false)
    private String name;

    private String shortName;
    private String country;
    private String logoUrl;
    private String venueName;
    private Integer venueCapacity;
}
