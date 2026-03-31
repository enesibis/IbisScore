package com.ibisscore.match.service;

import com.ibisscore.common.dto.TeamDTO;
import com.ibisscore.common.exception.ResourceNotFoundException;
import com.ibisscore.match.mapper.FixtureMapper;
import com.ibisscore.match.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final FixtureMapper  fixtureMapper;

    public TeamDTO getById(Long id) {
        return teamRepository.findById(id)
                .map(fixtureMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Takım", id));
    }

    public List<TeamDTO> getByLeague(Long leagueId) {
        return teamRepository.findByLeagueId(leagueId)
                .stream()
                .map(fixtureMapper::toDto)
                .toList();
    }
}
