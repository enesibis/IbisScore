package com.ibisscore.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ibisscore.common.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FixtureDTO {

    private Long id;
    private Integer apiId;

    private LeagueDTO league;
    private TeamDTO homeTeam;
    private TeamDTO awayTeam;

    private LocalDateTime matchDate;
    private MatchStatus status;

    // Skor
    private Integer homeGoals;
    private Integer awayGoals;
    private Integer homeGoalsHt;
    private Integer awayGoalsHt;

    // Tahmin (opsiyonel — eğer istenirse embed edilir)
    private PredictionDTO prediction;

    // En iyi oran (opsiyonel)
    private OddsDTO bestOdds;
}
