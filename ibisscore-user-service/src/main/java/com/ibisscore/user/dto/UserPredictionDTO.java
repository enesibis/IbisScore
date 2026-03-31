package com.ibisscore.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPredictionDTO {
    private Long id;
    private Long fixtureId;
    private String predictedResult;
    private Integer predictedHome;
    private Integer predictedAway;
    private Integer pointsEarned;
    private Boolean isCorrect;
    private LocalDateTime createdAt;
}
