package com.ibisscore.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
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
