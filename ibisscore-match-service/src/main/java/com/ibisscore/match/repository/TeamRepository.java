package com.ibisscore.match.repository;

import com.ibisscore.match.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByApiId(Integer apiId);

    @Query("""
            SELECT DISTINCT t FROM Team t
            JOIN Fixture f ON (f.homeTeam = t OR f.awayTeam = t)
            WHERE f.league.id = :leagueId
            ORDER BY t.name
            """)
    List<Team> findByLeagueId(@Param("leagueId") Long leagueId);
}
