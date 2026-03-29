package com.ibisscore.betting.controller;

import com.ibisscore.common.dto.ApiResponse;
import com.ibisscore.common.dto.ValueBetDTO;
import com.ibisscore.betting.service.ValueBetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/value-bets")
@RequiredArgsConstructor
public class ValueBetController {

    private final ValueBetService valueBetService;

    /**
     * GET /api/value-bets
     * Günün value bet listesi (en yüksek EV'ye göre sıralı)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ValueBetDTO>>> getDailyValueBets() {
        return ResponseEntity.ok(ApiResponse.success(valueBetService.getDailyValueBets()));
    }

    /**
     * GET /api/value-bets/fixture/{fixtureId}
     * Belirli bir maçın value bet analizi
     */
    @GetMapping("/fixture/{fixtureId}")
    public ResponseEntity<ApiResponse<ValueBetDTO>> getForFixture(
            @PathVariable Long fixtureId) {
        return ResponseEntity.ok(ApiResponse.success(
                valueBetService.getValueBetForFixture(fixtureId)));
    }
}
