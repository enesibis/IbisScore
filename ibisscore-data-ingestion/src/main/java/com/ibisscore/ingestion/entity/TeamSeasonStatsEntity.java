package com.ibisscore.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_season_stats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "league_id", "season"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TeamSeasonStatsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private LeagueEntity league;

    private Integer season;

    private Integer playedHome;
    private Integer playedAway;
    private Integer winsHome;
    private Integer winsAway;
    private Integer drawsHome;
    private Integer drawsAway;
    private Integer lossesHome;
    private Integer lossesAway;
    private Integer goalsForHome;
    private Integer goalsForAway;
    private Integer goalsAgainstHome;
    private Integer goalsAgainstAway;

    // Poisson lambda değerleri
    private Double avgGoalsForHome;
    private Double avgGoalsForAway;
    private Double avgGoalsAgainstHome;
    private Double avgGoalsAgainstAway;

    private Integer cleanSheetsHome;
    private Integer cleanSheetsAway;

    private String form;
    private Integer formPoints;
    private Integer leaguePosition;
    private Integer points;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
