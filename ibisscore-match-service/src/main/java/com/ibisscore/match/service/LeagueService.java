package com.ibisscore.match.service;

import com.ibisscore.common.dto.LeagueDTO;
import com.ibisscore.common.exception.ResourceNotFoundException;
import com.ibisscore.match.mapper.FixtureMapper;
import com.ibisscore.match.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final FixtureMapper    fixtureMapper;

    @Cacheable("leagues-active")
    public List<LeagueDTO> getActiveLeagues() {
        return leagueRepository.findByIsActiveTrue()
                .stream()
                .map(fixtureMapper::toDto)
                .toList();
    }

    public LeagueDTO getById(Long id) {
        return leagueRepository.findById(id)
                .map(fixtureMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Lig", id));
    }
}
