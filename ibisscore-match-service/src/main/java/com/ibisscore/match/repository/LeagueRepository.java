package com.ibisscore.match.repository;

import com.ibisscore.match.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {
    Optional<League> findByApiId(Integer apiId);
    List<League> findByIsActiveTrue();
}
