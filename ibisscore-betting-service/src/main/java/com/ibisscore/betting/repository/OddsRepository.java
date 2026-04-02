package com.ibisscore.betting.repository;

import com.ibisscore.common.dto.OddsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@RequiredArgsConstructor
public class OddsRepository {

    private final JdbcTemplate jdbc;

    /**
     * En yüksek home win odds'unu olan bookmaker'ı döndür
     * (kullanıcı için en avantajlı oran)
     */
    public OddsDTO findBestOddsForFixture(Long fixtureId) {
        String sql = """
                SELECT id, fixture_id, bookmaker,
                       home_win_odd, draw_odd, away_win_odd,
                       over25odd, under25odd,
                       btts_yes_odd, btts_no_odd, fetched_at
                FROM odds
                WHERE fixture_id = ?
                ORDER BY (home_win_odd + draw_odd + away_win_odd) DESC
                LIMIT 1
                """;

        try {
            return jdbc.queryForObject(sql, (rs, n) -> mapOdds(rs), fixtureId);
        } catch (Exception e) {
            return null;
        }
    }

    private OddsDTO mapOdds(ResultSet rs) throws SQLException {
        return OddsDTO.builder()
                .id(rs.getLong("id"))
                .fixtureId(rs.getLong("fixture_id"))
                .bookmaker(rs.getString("bookmaker"))
                .homeWinOdd(rs.getDouble("home_win_odd"))
                .drawOdd(rs.getDouble("draw_odd"))
                .awayWinOdd(rs.getDouble("away_win_odd"))
                .over25Odd(rs.getDouble("over25odd"))
                .under25Odd(rs.getDouble("under25odd"))
                .bttsYesOdd(rs.getDouble("btts_yes_odd"))
                .bttsNoOdd(rs.getDouble("btts_no_odd"))
                .build();
    }
}
