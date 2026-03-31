package com.ibisscore.match.controller;

import com.ibisscore.common.dto.ApiResponse;
import com.ibisscore.common.dto.TeamDTO;
import com.ibisscore.match.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getById(id)));
    }

    @GetMapping("/league/{leagueId}")
    public ResponseEntity<ApiResponse<List<TeamDTO>>> getByLeague(@PathVariable Long leagueId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getByLeague(leagueId)));
    }
}
