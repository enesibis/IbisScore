package com.ibisscore.ingestion.repository;

import com.ibisscore.common.enums.MatchStatus;
import com.ibisscore.ingestion.entity.FixtureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FixtureEntityRepository extends JpaRepository<FixtureEntity, Long> {
    Optional<FixtureEntity> findByApiId(Integer apiId);
    List<FixtureEntity> findByStatusAndMatchDateBetween(
            MatchStatus status, LocalDateTime start, LocalDateTime end);
}
