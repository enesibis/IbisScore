package com.ibisscore.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class PredictionDTO {

    private Long id;
    private Long fixtureId;
    private String modelVersion;

    // 1X2 olasılıkları (0.0 - 1.0)
    private Double homeWinProb;
    private Double drawProb;
    private Double awayWinProb;

    // Beklenen gol sayıları (Poisson lambda)
    private Double predictedHomeGoals;
    private Double predictedAwayGoals;

    // Ek marketler
    private Double over25Prob;
    private Double bttsProbability;

    // Model güveni (0.0 - 1.0)
    private Double confidenceScore;

    // Öneri: HOME_WIN, DRAW, AWAY_WIN, NO_BET
    private String recommendation;

    // Value bet analizi (Betting Service'ten gelir)
    private ValueBetDTO valueBet;

    private LocalDateTime createdAt;
}
