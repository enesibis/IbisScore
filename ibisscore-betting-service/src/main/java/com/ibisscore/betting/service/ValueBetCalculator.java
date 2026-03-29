package com.ibisscore.betting.service;

import com.ibisscore.common.dto.OddsDTO;
import com.ibisscore.common.dto.PredictionDTO;
import com.ibisscore.common.dto.ValueBetDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Value Bet Hesaplama Motoru
 *
 * Expected Value (EV) = (Model Olasılığı × Oran) - 1
 * Örnek: model %55 home win, oran 2.10
 *        EV = 0.55 × 2.10 - 1 = 0.155  → +15.5% value
 *
 * Kelly Criterion = (b×p - q) / b
 *   b = net kazanç (oran - 1)
 *   p = model olasılığı
 *   q = 1 - p
 * Quarter Kelly kullanılır (daha güvenli)
 */
@Slf4j
@Component
public class ValueBetCalculator {

    private static final double VALUE_BET_THRESHOLD = 0.05;     // Min %5 EV
    private static final double MIN_EDGE            = 0.03;     // Min %3 edge
    private static final double KELLY_FRACTION      = 0.25;     // Quarter Kelly

    public ValueBetDTO calculate(PredictionDTO prediction, OddsDTO odds) {
        if (odds == null || prediction == null) {
            return null;
        }

        // ─── Home Win ───────────────────────────────────────────
        double evHome   = calcEV(prediction.getHomeWinProb(), odds.getHomeWinOdd());
        double edgeHome = calcEdge(prediction.getHomeWinProb(), odds.getHomeWinOdd());
        double kellyHome = calcKelly(prediction.getHomeWinProb(), odds.getHomeWinOdd());

        // ─── Draw ────────────────────────────────────────────────
        double evDraw   = calcEV(prediction.getDrawProb(), odds.getDrawOdd());
        double edgeDraw = calcEdge(prediction.getDrawProb(), odds.getDrawOdd());
        double kellyDraw = calcKelly(prediction.getDrawProb(), odds.getDrawOdd());

        // ─── Away Win ────────────────────────────────────────────
        double evAway   = calcEV(prediction.getAwayWinProb(), odds.getAwayWinOdd());
        double edgeAway = calcEdge(prediction.getAwayWinProb(), odds.getAwayWinOdd());
        double kellyAway = calcKelly(prediction.getAwayWinProb(), odds.getAwayWinOdd());

        // ─── En iyi bet ──────────────────────────────────────────
        BestBetResult best = findBestBet(
                evHome, evDraw, evAway,
                odds.getHomeWinOdd(), odds.getDrawOdd(), odds.getAwayWinOdd(),
                kellyHome, kellyDraw, kellyAway
        );

        boolean isValueBetHome  = evHome  > VALUE_BET_THRESHOLD && edgeHome  > MIN_EDGE;
        boolean isValueBetDraw  = evDraw  > VALUE_BET_THRESHOLD && edgeDraw  > MIN_EDGE;
        boolean isValueBetAway  = evAway  > VALUE_BET_THRESHOLD && edgeAway  > MIN_EDGE;

        String confidenceLevel = determineConfidenceLevel(
                best.ev(), prediction.getConfidenceScore());

        return ValueBetDTO.builder()
                .fixtureId(prediction.getFixtureId())
                .evHome(round(evHome))
                .evDraw(round(evDraw))
                .evAway(round(evAway))
                .isValueBetHome(isValueBetHome)
                .isValueBetDraw(isValueBetDraw)
                .isValueBetAway(isValueBetAway)
                .edgeHome(round(edgeHome))
                .edgeDraw(round(edgeDraw))
                .edgeAway(round(edgeAway))
                .kellyHome(round(kellyHome * KELLY_FRACTION))
                .kellyDraw(round(kellyDraw * KELLY_FRACTION))
                .kellyAway(round(kellyAway * KELLY_FRACTION))
                .bestBet(best.name())
                .bestBetEv(round(best.ev()))
                .bestBetOdd(best.odd())
                .bestBetKelly(round(best.kelly() * KELLY_FRACTION))
                .confidenceLevel(confidenceLevel)
                .build();
    }

    /**
     * EV = (p × odd) - 1
     * Pozitif EV → potansiyel value bet
     */
    private double calcEV(Double prob, Double odd) {
        if (prob == null || odd == null || prob <= 0 || odd <= 0) return -1.0;
        return (prob * odd) - 1.0;
    }

    /**
     * Edge = model olasılığı - bookmaker'ın implied olasılığı
     * Bookmaker implied prob = 1/odd
     */
    private double calcEdge(Double prob, Double odd) {
        if (prob == null || odd == null || odd <= 0) return -1.0;
        double impliedProb = 1.0 / odd;
        return prob - impliedProb;
    }

    /**
     * Kelly = (b×p - q) / b   where b = odd - 1
     */
    private double calcKelly(Double prob, Double odd) {
        if (prob == null || odd == null || odd <= 1) return 0.0;
        double b = odd - 1.0;
        double q = 1.0 - prob;
        double kelly = (b * prob - q) / b;
        return Math.max(0.0, kelly);  // Negatif kelly = no bet
    }

    private BestBetResult findBestBet(
            double evHome, double evDraw, double evAway,
            Double oddHome, Double oddDraw, Double oddAway,
            double kellyHome, double kellyDraw, double kellyAway) {

        double maxEv = Math.max(evHome, Math.max(evDraw, evAway));

        if (maxEv <= VALUE_BET_THRESHOLD) {
            return new BestBetResult("NO_BET", -1.0, 0.0, 0.0);
        }

        if (maxEv == evHome) {
            return new BestBetResult("HOME_WIN", evHome,
                    oddHome != null ? oddHome : 0, kellyHome);
        } else if (maxEv == evDraw) {
            return new BestBetResult("DRAW", evDraw,
                    oddDraw != null ? oddDraw : 0, kellyDraw);
        } else {
            return new BestBetResult("AWAY_WIN", evAway,
                    oddAway != null ? oddAway : 0, kellyAway);
        }
    }

    private String determineConfidenceLevel(double ev, Double modelConfidence) {
        double confidence = modelConfidence != null ? modelConfidence : 0.5;
        if (ev > 0.15 && confidence > 0.75) return "HIGH";
        if (ev > 0.08 && confidence > 0.65) return "MEDIUM";
        return "LOW";
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private record BestBetResult(String name, double ev, double odd, double kelly) {}
}
