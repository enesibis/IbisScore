package com.ibisscore.betting.service;

import com.ibisscore.common.dto.OddsDTO;
import com.ibisscore.common.dto.PredictionDTO;
import com.ibisscore.common.dto.ValueBetDTO;
import com.ibisscore.betting.repository.OddsRepository;
import com.ibisscore.betting.repository.PredictionViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValueBetService {

    private final ValueBetCalculator calculator;
    private final OddsRepository oddsRepository;
    private final PredictionViewRepository predictionRepository;

    /**
     * Günün en iyi value bet'lerini döndürür.
     * Cache TTL: 30 dakika
     */
    @Cacheable(value = "daily-value-bets")
    public List<ValueBetDTO> getDailyValueBets() {
        List<PredictionDTO> predictions = predictionRepository.findTodayPredictions();
        List<ValueBetDTO> valueBets = new ArrayList<>();

        for (PredictionDTO prediction : predictions) {
            OddsDTO bestOdds = oddsRepository.findBestOddsForFixture(prediction.getFixtureId());
            if (bestOdds == null) continue;

            ValueBetDTO vb = calculator.calculate(prediction, bestOdds);
            if (vb != null && !"NO_BET".equals(vb.getBestBet())) {
                valueBets.add(vb);
            }
        }

        // EV'ye göre sırala (yüksekten düşüğe)
        valueBets.sort(Comparator.comparingDouble(ValueBetDTO::getBestBetEv).reversed());

        log.info("Found {} value bets for today", valueBets.size());
        return valueBets;
    }

    public ValueBetDTO getValueBetForFixture(Long fixtureId) {
        PredictionDTO prediction = predictionRepository.findByFixtureId(fixtureId);
        if (prediction == null) {
            throw new IllegalArgumentException("Bu maç için tahmin bulunamadı: " + fixtureId);
        }

        OddsDTO bestOdds = oddsRepository.findBestOddsForFixture(fixtureId);
        if (bestOdds == null) {
            throw new IllegalArgumentException("Bu maç için oran verisi bulunamadı: " + fixtureId);
        }

        return calculator.calculate(prediction, bestOdds);
    }
}
