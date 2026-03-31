"""
Ensemble Tahmin Servisi.

Poisson (%40) + XGBoost (%60) ağırlıklı birleşim.
XGBoost yüklü değilse sadece Poisson kullanılır.
"""

import logging
from app.models.poisson_model import PoissonModel
from app.models.xgboost_model import XGBoostModel
from app.config import settings

logger = logging.getLogger(__name__)

CONFIDENCE_THRESHOLD_HIGH   = 0.75
CONFIDENCE_THRESHOLD_MEDIUM = 0.65


class EnsemblePredictor:

    def __init__(self, poisson: PoissonModel, xgboost: XGBoostModel):
        self.poisson  = poisson
        self.xgboost  = xgboost

    def predict(self, features: dict) -> dict:
        """
        Feature dict'inden ensemble tahmini üretir.
        """
        # ── Poisson ─────────────────────────────────────────────
        poisson_result = self.poisson.predict(
            home_attack  = features.get("_home_attack",  1.2),
            home_defense = features.get("_home_defense", 1.0),
            away_attack  = features.get("_away_attack",  1.0),
            away_defense = features.get("_away_defense", 1.2),
        )

        p_home = poisson_result["home_win_prob"]
        p_draw = poisson_result["draw_prob"]
        p_away = poisson_result["away_win_prob"]

        # ── XGBoost ─────────────────────────────────────────────
        xgb_result = self.xgboost.predict(features)
        xgb_loaded = xgb_result is not None

        if xgb_loaded:
            w_p = settings.poisson_weight
            w_x = settings.xgboost_weight
            home_win_prob = w_p * p_home + w_x * xgb_result["home_win_prob"]
            draw_prob     = w_p * p_draw + w_x * xgb_result["draw_prob"]
            away_win_prob = w_p * p_away + w_x * xgb_result["away_win_prob"]
            model_version = "ensemble-v1"
        else:
            home_win_prob = p_home
            draw_prob     = p_draw
            away_win_prob = p_away
            model_version = "poisson-v1"

        # Normalize
        total = home_win_prob + draw_prob + away_win_prob
        home_win_prob /= total
        draw_prob     /= total
        away_win_prob /= total

        # ── Güven skoru ──────────────────────────────────────────
        # Max olasılık ne kadar yüksekse model o kadar emin
        max_prob        = max(home_win_prob, draw_prob, away_win_prob)
        entropy         = self._entropy([home_win_prob, draw_prob, away_win_prob])
        confidence      = round(max_prob * (1 - entropy / 1.0986), 4)  # 1.0986 = ln(3)
        confidence      = max(0.0, min(1.0, confidence))

        # ── Öneri ────────────────────────────────────────────────
        recommendation = self._recommend(
            home_win_prob, draw_prob, away_win_prob, confidence)

        return {
            "model_version":        model_version,
            "home_win_prob":        round(home_win_prob, 4),
            "draw_prob":            round(draw_prob, 4),
            "away_win_prob":        round(away_win_prob, 4),
            "predicted_home_goals": poisson_result["predicted_home_goals"],
            "predicted_away_goals": poisson_result["predicted_away_goals"],
            "over_2_5_prob":        poisson_result["over_2_5_prob"],
            "btts_probability":     poisson_result["btts_probability"],
            "confidence_score":     confidence,
            "recommendation":       recommendation,
            "score_matrix":         poisson_result["score_matrix"],
        }

    def _entropy(self, probs: list[float]) -> float:
        import math
        return -sum(p * math.log(p + 1e-10) for p in probs)

    def _recommend(self, home: float, draw: float, away: float,
                   confidence: float) -> str:
        """
        Minimum güven eşiğini geçen en yüksek olasılıklı sonucu öner.
        Eşiği geçemiyorsa NO_BET döner.
        """
        if confidence < 0.55:
            return "NO_BET"

        best_prob = max(home, draw, away)
        if best_prob == home:
            return "HOME_WIN"
        elif best_prob == draw:
            return "DRAW"
        else:
            return "AWAY_WIN"
