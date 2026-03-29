package com.ibisscore.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValueBetDTO {

    private Long fixtureId;

    // 1X2 Expected Value'ları
    private Double evHome;
    private Double evDraw;
    private Double evAway;

    // Value bet var mı?
    private Boolean isValueBetHome;
    private Boolean isValueBetDraw;
    private Boolean isValueBetAway;

    // Edge (model olasılığı - implied olasılık)
    private Double edgeHome;
    private Double edgeDraw;
    private Double edgeAway;

    // Kelly Criterion (önerilen stake oranı)
    private Double kellyHome;
    private Double kellyDraw;
    private Double kellyAway;

    // En iyi value bet
    private String bestBet;           // "HOME_WIN", "DRAW", "AWAY_WIN"
    private Double bestBetEv;
    private Double bestBetOdd;
    private Double bestBetKelly;

    // Güven seviyesi: LOW, MEDIUM, HIGH
    private String confidenceLevel;
}
