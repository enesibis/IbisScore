package com.ibisscore.betting.repository;

import com.ibisscore.common.dto.PredictionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PredictionViewRepository {

    private final JdbcTemplate jdbc;

    public List<PredictionDTO> findTodayPredictions() {
        String sql = """
                SELECT p.id, p.fixture_id, p.model_version,
                       p.home_win_prob, p.draw_prob, p.away_win_prob,
                       p.predicted_home_goals, p.predicted_away_goals,
                       p.over25prob, p.btts_prob,
                       p.confidence_score, p.recommendation
                FROM predictions p
                JOIN fixtures f ON f.id = p.fixture_id
                WHERE DATE(f.match_date) = CURRENT_DATE
                AND f.status = 'NS'
                ORDER BY p.confidence_score DESC
                """;

        return jdbc.query(sql, (rs, n) -> mapPrediction(rs));
    }

    public PredictionDTO findByFixtureId(Long fixtureId) {
        String sql = """
                SELECT id, fixture_id, model_version,
                       home_win_prob, draw_prob, away_win_prob,
                       predicted_home_goals, predicted_away_goals,
                       over25prob, btts_prob,
                       confidence_score, recommendation
                FROM predictions
                WHERE fixture_id = ?
                ORDER BY created_at DESC
                LIMIT 1
                """;

        try {
            return jdbc.queryForObject(sql, (rs, n) -> mapPrediction(rs), fixtureId);
        } catch (Exception e) {
            return null;
        }
    }

    private PredictionDTO mapPrediction(ResultSet rs) throws SQLException {
        return PredictionDTO.builder()
                .id(rs.getLong("id"))
                .fixtureId(rs.getLong("fixture_id"))
                .modelVersion(rs.getString("model_version"))
                .homeWinProb(rs.getDouble("home_win_prob"))
                .drawProb(rs.getDouble("draw_prob"))
                .awayWinProb(rs.getDouble("away_win_prob"))
                .predictedHomeGoals(rs.getDouble("predicted_home_goals"))
                .predictedAwayGoals(rs.getDouble("predicted_away_goals"))
                .over25Prob(rs.getDouble("over25prob"))
                .bttsProbability(rs.getDouble("btts_prob"))
                .confidenceScore(rs.getDouble("confidence_score"))
                .recommendation(rs.getString("recommendation"))
                .build();
    }
}
