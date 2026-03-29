package com.ibisscore.match.service;

import com.ibisscore.common.dto.FixtureDTO;
import com.ibisscore.common.enums.MatchStatus;
import com.ibisscore.common.exception.ResourceNotFoundException;
import com.ibisscore.match.entity.Fixture;
import com.ibisscore.match.mapper.FixtureMapper;
import com.ibisscore.match.repository.FixtureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FixtureService {

    private final FixtureRepository fixtureRepository;
    private final FixtureMapper fixtureMapper;

    @Cacheable(value = "fixtures-today", key = "#date.toString()")
    public List<FixtureDTO> getFixturesByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end   = date.atTime(23, 59, 59);
        List<Fixture> fixtures = fixtureRepository.findByDateRange(start, end);
        return fixtureMapper.toDtoList(fixtures);
    }

    @Cacheable(value = "fixture-detail", key = "#id", unless = "#result.status.name() == 'LIVE'")
    public FixtureDTO getFixtureById(Long id) {
        Fixture fixture = fixtureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maç", id));
        return fixtureMapper.toDto(fixture);
    }

    @Cacheable(value = "live-fixtures")
    public List<FixtureDTO> getLiveFixtures() {
        return fixtureMapper.toDtoList(
                fixtureRepository.findByStatusIn(List.of(MatchStatus.LIVE, MatchStatus.HT))
        );
    }

    @Cacheable(value = "fixtures-league", key = "#leagueId + '-' + #pageable.pageNumber")
    public Page<FixtureDTO> getFixturesByLeague(Long leagueId, Pageable pageable) {
        return fixtureRepository.findByLeagueId(leagueId, pageable)
                .map(fixtureMapper::toDto);
    }

    public Page<FixtureDTO> getFixturesByTeam(Long teamId, Pageable pageable) {
        return fixtureRepository.findByTeamId(teamId, pageable)
                .map(fixtureMapper::toDto);
    }
}
