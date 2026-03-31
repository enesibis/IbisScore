"""
Feature Engineering Servisi.

Veritabanındaki ham istatistiklerden ML modeli için
feature vektörü oluşturur.
"""

from sqlalchemy.orm import Session
from sqlalchemy import text
import logging

logger = logging.getLogger(__name__)


class FeatureBuilder:

    def build(self, fixture_id: int, db: Session) -> dict | None:
        """
        Verilen maç ID'si için feature dict'i oluşturur.
        Eksik veri varsa None döner.
        """
        fixture = self._get_fixture(fixture_id, db)
        if not fixture:
            logger.warning("Fixture not found: %s", fixture_id)
            return None

        home_id    = fixture["home_team_id"]
        away_id    = fixture["away_team_id"]
        league_id  = fixture["league_id"]
        season     = 2024

        home_stats = self._get_team_stats(home_id, league_id, season, db)
        away_stats = self._get_team_stats(away_id, league_id, season, db)
        odds       = self._get_best_odds(fixture_id, db)
        h2h        = self._get_h2h(home_id, away_id, db)

        if not home_stats or not away_stats:
            logger.warning("Missing team stats for fixture %s", fixture_id)
            return None

        return {
            # Gol istatistikleri (iç/dış saha bazlı)
            "home_avg_goals_for":     home_stats.get("avg_goals_for_home", 1.2),
            "home_avg_goals_against": home_stats.get("avg_goals_against_home", 1.0),
            "away_avg_goals_for":     away_stats.get("avg_goals_for_away", 1.0),
            "away_avg_goals_against": away_stats.get("avg_goals_against_away", 1.2),

            # Form (0-15 puan)
            "home_form_points": home_stats.get("form_points", 7),
            "away_form_points": away_stats.get("form_points", 7),

            # İç/Dış saha kazanma oranları
            "home_home_win_rate": self._win_rate(
                home_stats.get("wins_home", 0),
                home_stats.get("played_home", 1)
            ),
            "away_away_win_rate": self._win_rate(
                away_stats.get("wins_away", 0),
                away_stats.get("played_away", 1)
            ),

            # Güç farkı
            "attack_diff":  home_stats.get("avg_goals_for_home", 1.2)  -
                            away_stats.get("avg_goals_for_away", 1.0),
            "defense_diff": away_stats.get("avg_goals_against_away", 1.2) -
                            home_stats.get("avg_goals_against_home", 1.0),

            # Head-to-head (son 5 karşılaşma)
            "h2h_home_wins":  h2h.get("home_wins", 2),
            "h2h_draws":      h2h.get("draws", 1),
            "h2h_away_wins":  h2h.get("away_wins", 2),
            "h2h_avg_goals":  h2h.get("avg_goals", 2.5),

            # Bookmaker implied olasılıkları
            "implied_prob_home": self._implied_prob(odds.get("home_win_odd", 2.5)),
            "implied_prob_draw": self._implied_prob(odds.get("draw_odd", 3.2)),
            "implied_prob_away": self._implied_prob(odds.get("away_win_odd", 2.8)),

            # Dinlenme süresi (gün)
            "days_rest_home": self._get_days_rest(home_id, fixture["match_date"], db),
            "days_rest_away": self._get_days_rest(away_id, fixture["match_date"], db),

            # Lig sırası farkı
            "league_position_diff": (
                home_stats.get("league_position", 10) -
                away_stats.get("league_position", 10)
            ),

            # Ham lambda değerleri (Poisson için)
            "_home_attack":  home_stats.get("avg_goals_for_home", 1.2),
            "_home_defense": home_stats.get("avg_goals_against_home", 1.0),
            "_away_attack":  away_stats.get("avg_goals_for_away", 1.0),
            "_away_defense": away_stats.get("avg_goals_against_away", 1.2),
        }

    def _get_fixture(self, fixture_id: int, db: Session) -> dict | None:
        row = db.execute(text("""
            SELECT id, home_team_id, away_team_id, league_id, match_date
            FROM fixtures WHERE id = :id
        """), {"id": fixture_id}).fetchone()
        return dict(row._mapping) if row else None

    def _get_team_stats(self, team_id: int, league_id: int,
                        season: int, db: Session) -> dict | None:
        row = db.execute(text("""
            SELECT avg_goals_for_home, avg_goals_for_away,
                   avg_goals_against_home, avg_goals_against_away,
                   wins_home, wins_away, played_home, played_away,
                   form_points, league_position
            FROM team_season_stats
            WHERE team_id = :team_id AND league_id = :league_id AND season = :season
        """), {"team_id": team_id, "league_id": league_id, "season": season}).fetchone()
        return dict(row._mapping) if row else None

    def _get_best_odds(self, fixture_id: int, db: Session) -> dict:
        row = db.execute(text("""
            SELECT home_win_odd, draw_odd, away_win_odd
            FROM odds WHERE fixture_id = :fid
            ORDER BY fetched_at DESC LIMIT 1
        """), {"fid": fixture_id}).fetchone()
        return dict(row._mapping) if row else {}

    def _get_h2h(self, home_id: int, away_id: int, db: Session) -> dict:
        rows = db.execute(text("""
            SELECT result, home_goals, away_goals
            FROM head_to_head
            WHERE (home_team_id = :h AND away_team_id = :a)
               OR (home_team_id = :a AND away_team_id = :h)
            ORDER BY match_date DESC LIMIT 5
        """), {"h": home_id, "a": away_id}).fetchall()

        if not rows:
            return {}

        home_wins, draws, away_wins, total_goals = 0, 0, 0, 0
        for r in rows:
            if r.result == "HOME_WIN":
                home_wins += 1
            elif r.result == "DRAW":
                draws += 1
            else:
                away_wins += 1
            total_goals += (r.home_goals or 0) + (r.away_goals or 0)

        return {
            "home_wins": home_wins,
            "draws":     draws,
            "away_wins": away_wins,
            "avg_goals": round(total_goals / len(rows), 2),
        }

    def _get_days_rest(self, team_id: int, match_date, db: Session) -> int:
        row = db.execute(text("""
            SELECT match_date FROM fixtures
            WHERE (home_team_id = :tid OR away_team_id = :tid)
              AND match_date < :md AND status = 'FT'
            ORDER BY match_date DESC LIMIT 1
        """), {"tid": team_id, "md": match_date}).fetchone()

        if not row:
            return 7
        delta = match_date - row.match_date
        return min(delta.days, 30)

    def _win_rate(self, wins: int, played: int) -> float:
        if played == 0:
            return 0.4
        return round(wins / played, 3)

    def _implied_prob(self, odd: float | None) -> float:
        if not odd or odd <= 0:
            return 0.33
        return round(1.0 / odd, 4)
