package com.ibisscore.match.messaging;

import com.ibisscore.common.event.FixtureEvent;
import com.ibisscore.match.config.RabbitConfig;
import com.ibisscore.match.service.PredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FixtureEventListener {

    private final PredictionService predictionService;

    @RabbitListener(queues = RabbitConfig.FIXTURES_QUEUE)
    public void handleFixtureEvent(FixtureEvent event) {
        log.info("Received fixture event: type={}, fixtureId={}", event.getType(), event.getFixtureId());

        if (event.getType() == FixtureEvent.EventType.FIXTURE_CREATED
                || event.getType() == FixtureEvent.EventType.FIXTURE_UPDATED) {
            try {
                predictionService.requestPrediction(event.getFixtureId());
            } catch (Exception e) {
                log.error("Failed to request prediction for fixture {}: {}", event.getFixtureId(), e.getMessage());
                throw e; // triggers retry / DLQ
            }
        }
    }
}
