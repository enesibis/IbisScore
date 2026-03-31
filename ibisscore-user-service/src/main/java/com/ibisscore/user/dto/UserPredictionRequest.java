package com.ibisscore.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserPredictionRequest {

    @NotNull
    private Long fixtureId;

    @NotBlank
    @Pattern(regexp = "^[1X2]$", message = "Tahmin '1', 'X' veya '2' olmalıdır")
    private String predictedResult;

    private Integer predictedHome;
    private Integer predictedAway;
}
