package com.ibisscore.match.controller;

import com.ibisscore.common.dto.ApiResponse;
import com.ibisscore.common.dto.PredictionDTO;
import com.ibisscore.match.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @GetMapping("/fixture/{fixtureId}")
    public ResponseEntity<ApiResponse<List<PredictionDTO>>> getByFixture(
            @PathVariable Long fixtureId) {
        return ResponseEntity.ok(ApiResponse.success(
                predictionService.getPredictionsByFixture(fixtureId)));
    }

    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<PredictionDTO>>> getTopPredictions(
            @RequestParam(defaultValue = "0.70") Double minConfidence,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                predictionService.getTopPredictions(minConfidence, limit)));
    }

    @PostMapping("/fixture/{fixtureId}/request")
    public ResponseEntity<ApiResponse<String>> requestPrediction(
            @PathVariable Long fixtureId) {
        predictionService.requestPrediction(fixtureId);
        return ResponseEntity.ok(ApiResponse.success("ok", "Tahmin isteği gönderildi"));
    }
}
