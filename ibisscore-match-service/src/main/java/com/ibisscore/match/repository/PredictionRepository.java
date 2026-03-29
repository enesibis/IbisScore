package com.ibisscore.match.repository;

import com.ibisscore.match.entity.Prediction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    Optional<Prediction> findByFixtureIdAndModelVersion(Long fixtureId, String modelVersion);

    List<Prediction> findByFixtureId(Long fixtureId);

    @Query("""
            SELECT p FROM Prediction p
            JOIN FETCH p.fixture f
            JOIN FETCH f.homeTeam
            JOIN FETCH f.awayTeam
            JOIN FETCH f.league
            WHERE f.status = 'NS'
            AND p.confidenceScore >= :minConfidence
            ORDER BY p.confidenceScore DESC
            """)
    List<Prediction> findTopConfidentPredictions(
            @Param("minConfidence") Double minConfidence,
            Pageable pageable);
}
