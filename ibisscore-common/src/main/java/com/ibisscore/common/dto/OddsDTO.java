package com.ibisscore.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OddsDTO {
    private Long id;
    private Long fixtureId;
    private String bookmaker;
    private Double homeWinOdd;
    private Double drawOdd;
    private Double awayWinOdd;
    private Double over25Odd;
    private Double under25Odd;
    private Double bttsYesOdd;
    private Double bttsNoOdd;
    private LocalDateTime fetchedAt;
}
