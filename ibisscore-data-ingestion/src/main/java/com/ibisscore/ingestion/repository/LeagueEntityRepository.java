package com.ibisscore.ingestion.repository;

import com.ibisscore.ingestion.entity.LeagueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LeagueEntityRepository extends JpaRepository<LeagueEntity, Long> {
    Optional<LeagueEntity> findByApiId(Integer apiId);
}
