package com.ibisscore.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardEntryDTO {
    private Long userId;
    private String username;
    private Long totalPoints;
    private Long correctPredictions;
    private Long totalPredictions;
}
