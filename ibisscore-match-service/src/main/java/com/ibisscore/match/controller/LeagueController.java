package com.ibisscore.match.controller;

import com.ibisscore.common.dto.ApiResponse;
import com.ibisscore.common.dto.LeagueDTO;
import com.ibisscore.match.service.LeagueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leagues")
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeagueDTO>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(leagueService.getActiveLeagues()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeagueDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leagueService.getById(id)));
    }
}
