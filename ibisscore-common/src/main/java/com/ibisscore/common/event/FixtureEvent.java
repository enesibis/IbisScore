package com.ibisscore.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RabbitMQ üzerinden servisler arası iletişim için event nesnesi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixtureEvent {

    public enum EventType {
        FIXTURE_CREATED,
        FIXTURE_UPDATED,
        SCORE_UPDATED,
        PREDICTION_REQUESTED,
        PREDICTION_COMPLETED
    }

    private EventType type;
    private Long fixtureId;
    private Integer apiId;
    private LocalDateTime occurredAt;
    private String payload;  // JSON string (ek veri için)
}
