package com.ibisscore.match.service;

import com.ibisscore.common.dto.PredictionDTO;
import com.ibisscore.common.event.FixtureEvent;
import com.ibisscore.common.exception.ResourceNotFoundException;
import com.ibisscore.match.client.AiServiceClient;
import com.ibisscore.match.entity.Fixture;
import com.ibisscore.match.entity.Prediction;
import com.ibisscore.match.mapper.PredictionMapper;
import com.ibisscore.match.repository.FixtureRepository;
import com.ibisscore.match.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PredictionService {

    private final PredictionRepository predictionRepository;
    private final FixtureRepository fixtureRepository;
    private final PredictionMapper predictionMapper;
    private final RabbitTemplate rabbitTemplate;
    private final AiServiceClient aiServiceClient;

    public List<PredictionDTO> getPredictionsByFixture(Long fixtureId) {
        List<Prediction> predictions = predictionRepository.findByFixtureId(fixtureId);
        return predictionMapper.toDtoList(predictions);
    }

    @Cacheable(value = "top-predictions", key = "#minConfidence + '-' + #limit")
    public List<PredictionDTO> getTopPredictions(Double minConfidence, int limit) {
        return predictionMapper.toDtoList(
                predictionRepository.findTopConfidentPredictions(
                        minConfidence, PageRequest.of(0, limit))
        );
    }

    public void requestPrediction(Long fixtureId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new ResourceNotFoundException("Maç", fixtureId));

        FixtureEvent event = FixtureEvent.builder()
                .type(FixtureEvent.EventType.PREDICTION_REQUESTED)
                .fixtureId(fixture.getId())
                .apiId(fixture.getApiId())
                .occurredAt(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                "ibisscore.predictions",
                "prediction.request",
                event
        );
        log.info("Prediction request sent for fixture: {}", fixtureId);
    }

    @Transactional
    public void savePrediction(PredictionDTO dto) {
        Fixture fixture = fixtureRepository.findById(dto.getFixtureId())
                .orElseThrow(() -> new ResourceNotFoundException("Maç", dto.getFixtureId()));

        Prediction prediction = predictionRepository
                .findByFixtureIdAndModelVersion(dto.getFixtureId(), dto.getModelVersion())
                .orElse(new Prediction());

        prediction.setFixture(fixture);
        prediction.setModelVersion(dto.getModelVersion());
        prediction.setHomeWinProb(dto.getHomeWinProb());
        prediction.setDrawProb(dto.getDrawProb());
        prediction.setAwayWinProb(dto.getAwayWinProb());
        prediction.setPredictedHomeGoals(dto.getPredictedHomeGoals());
        prediction.setPredictedAwayGoals(dto.getPredictedAwayGoals());
        prediction.setOver25Prob(dto.getOver25Prob());
        prediction.setBttsProb(dto.getBttsProbability());
        prediction.setConfidenceScore(dto.getConfidenceScore());
        prediction.setRecommendation(dto.getRecommendation());

        predictionRepository.save(prediction);
        log.info("Prediction saved for fixture {} model {}", dto.getFixtureId(), dto.getModelVersion());
    }
}
