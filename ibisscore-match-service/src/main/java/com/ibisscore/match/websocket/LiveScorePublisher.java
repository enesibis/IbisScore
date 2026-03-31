package com.ibisscore.match.websocket;

import com.ibisscore.common.event.FixtureEvent;
import com.ibisscore.match.config.RabbitConfig;
import com.ibisscore.match.entity.Fixture;
import com.ibisscore.match.repository.FixtureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * RabbitMQ'dan SCORE_UPDATED event'ini dinler ve
 * WebSocket üzerinden /topic/scores/{fixtureId} kanalına broadcast eder.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LiveScorePublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final FixtureRepository fixtureRepository;

    @RabbitListener(queues = RabbitConfig.FIXTURES_QUEUE,
                    id      = "liveScoreListener",
                    returnExceptions = "false")
    public void onScoreUpdated(FixtureEvent event) {
        if (event.getType() != FixtureEvent.EventType.SCORE_UPDATED) {
            return;
        }

        fixtureRepository.findById(event.getFixtureId()).ifPresent(fixture -> {
            Map<String, Object> payload = buildPayload(fixture);
            String destination = "/topic/scores/" + fixture.getId();
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("Score update broadcast → {} : {}:{}", destination,
                    fixture.getHomeGoals(), fixture.getAwayGoals());
        });
    }

    private Map<String, Object> buildPayload(Fixture f) {
        return Map.of(
                "fixtureId",  f.getId(),
                "homeGoals",  f.getHomeGoals()   != null ? f.getHomeGoals()   : 0,
                "awayGoals",  f.getAwayGoals()   != null ? f.getAwayGoals()   : 0,
                "homeGoalsHt",f.getHomeGoalsHt() != null ? f.getHomeGoalsHt() : 0,
                "awayGoalsHt",f.getAwayGoalsHt() != null ? f.getAwayGoalsHt() : 0,
                "status",     f.getStatus()      != null ? f.getStatus().name(): "UNKNOWN"
        );
    }
}
