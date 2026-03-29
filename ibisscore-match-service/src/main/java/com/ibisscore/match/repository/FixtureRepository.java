package com.ibisscore.match.repository;

import com.ibisscore.common.enums.MatchStatus;
import com.ibisscore.match.entity.Fixture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    Optional<Fixture> findByApiId(Integer apiId);

    @Query("""
            SELECT f FROM Fixture f
            JOIN FETCH f.homeTeam
            JOIN FETCH f.awayTeam
            JOIN FETCH f.league
            WHERE f.matchDate BETWEEN :start AND :end
            ORDER BY f.matchDate ASC
            """)
    List<Fixture> findByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            SELECT f FROM Fixture f
            JOIN FETCH f.homeTeam
            JOIN FETCH f.awayTeam
            JOIN FETCH f.league
            WHERE f.status IN :statuses
            ORDER BY f.matchDate ASC
            """)
    List<Fixture> findByStatusIn(@Param("statuses") List<MatchStatus> statuses);

    @Query("""
            SELECT f FROM Fixture f
            JOIN FETCH f.homeTeam ht
            JOIN FETCH f.awayTeam at
            JOIN FETCH f.league l
            WHERE l.id = :leagueId
            ORDER BY f.matchDate DESC
            """)
    Page<Fixture> findByLeagueId(@Param("leagueId") Long leagueId, Pageable pageable);

    @Query("""
            SELECT f FROM Fixture f
            JOIN FETCH f.homeTeam
            JOIN FETCH f.awayTeam
            JOIN FETCH f.league
            WHERE (f.homeTeam.id = :teamId OR f.awayTeam.id = :teamId)
            ORDER BY f.matchDate DESC
            """)
    Page<Fixture> findByTeamId(@Param("teamId") Long teamId, Pageable pageable);

    @Query("""
            SELECT f FROM Fixture f
            WHERE f.status = :status
            AND f.matchDate BETWEEN :start AND :end
            """)
    List<Fixture> findLiveOrUpcoming(
            @Param("status") MatchStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
