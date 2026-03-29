package com.ibisscore.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "odds",
       uniqueConstraints = @UniqueConstraint(columnNames = {"fixture_id", "bookmaker"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OddsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private FixtureEntity fixture;

    @Column(nullable = false, length = 50)
    private String bookmaker;

    private Double homeWinOdd;
    private Double drawOdd;
    private Double awayWinOdd;
    private Double over25Odd;
    private Double under25Odd;
    private Double bttsYesOdd;
    private Double bttsNoOdd;

    private LocalDateTime fetchedAt;
}
