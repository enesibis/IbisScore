package com.ibisscore.ingestion.repository;

import com.ibisscore.ingestion.entity.TeamSeasonStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TeamSeasonStatsRepository extends JpaRepository<TeamSeasonStatsEntity, Long> {
    Optional<TeamSeasonStatsEntity> findByTeamIdAndLeagueIdAndSeason(
            Long teamId, Long leagueId, Integer season);
}
