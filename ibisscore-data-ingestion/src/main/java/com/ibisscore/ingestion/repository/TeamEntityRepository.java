package com.ibisscore.ingestion.repository;

import com.ibisscore.ingestion.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TeamEntityRepository extends JpaRepository<TeamEntity, Long> {
    Optional<TeamEntity> findByApiId(Integer apiId);
}
