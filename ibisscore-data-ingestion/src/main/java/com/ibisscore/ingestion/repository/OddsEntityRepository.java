package com.ibisscore.ingestion.repository;

import com.ibisscore.ingestion.entity.OddsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OddsEntityRepository extends JpaRepository<OddsEntity, Long> {
    Optional<OddsEntity> findByFixtureIdAndBookmaker(Long fixtureId, String bookmaker);
}
