package com.ibisscore.match.controller;

import com.ibisscore.common.dto.ApiResponse;
import com.ibisscore.common.dto.FixtureDTO;
import com.ibisscore.match.service.FixtureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/fixtures")
@RequiredArgsConstructor
public class FixtureController {

    private final FixtureService fixtureService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FixtureDTO>>> getByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate target = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(fixtureService.getFixturesByDate(target)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FixtureDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(fixtureService.getFixtureById(id)));
    }

    @GetMapping("/live")
    public ResponseEntity<ApiResponse<List<FixtureDTO>>> getLive() {
        return ResponseEntity.ok(ApiResponse.success(fixtureService.getLiveFixtures()));
    }

    @GetMapping("/league/{leagueId}")
    public ResponseEntity<ApiResponse<Page<FixtureDTO>>> getByLeague(
            @PathVariable Long leagueId,
            @PageableDefault(size = 20, sort = "matchDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                fixtureService.getFixturesByLeague(leagueId, pageable)));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<Page<FixtureDTO>>> getByTeam(
            @PathVariable Long teamId,
            @PageableDefault(size = 20, sort = "matchDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                fixtureService.getFixturesByTeam(teamId, pageable)));
    }
}
