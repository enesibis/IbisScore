package com.ibisscore.ingestion.controller;

import com.ibisscore.common.dto.ApiResponse;
import com.ibisscore.ingestion.scheduler.FootballDataScheduler;
import com.ibisscore.ingestion.service.FixtureIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final FootballDataScheduler scheduler;
    private final FixtureIngestionService fixtureService;

    @PostMapping("/trigger/fixtures")
    public ResponseEntity<ApiResponse<String>> triggerFixtures() {
        scheduler.fetchDailyFixtures();
        return ResponseEntity.ok(ApiResponse.success("Fixtures fetch triggered", "OK"));
    }

    @PostMapping("/trigger/fixtures/{date}")
    public ResponseEntity<ApiResponse<String>> triggerFixturesByDate(@PathVariable String date) {
        fixtureService.fetchAndSaveFixtures(LocalDate.parse(date));
        return ResponseEntity.ok(ApiResponse.success("Fixtures fetch triggered for " + date, "OK"));
    }

    @PostMapping("/trigger/odds")
    public ResponseEntity<ApiResponse<String>> triggerOdds() {
        scheduler.fetchUpcomingOdds();
        return ResponseEntity.ok(ApiResponse.success("Odds fetch triggered", "OK"));
    }

    @PostMapping("/trigger/team-stats")
    public ResponseEntity<ApiResponse<String>> triggerTeamStats() {
        scheduler.fetchTeamStats();
        return ResponseEntity.ok(ApiResponse.success("Team stats fetch triggered", "OK"));
    }
}
