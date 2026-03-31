"""
Poisson Gol Tahmin Modeli — Dixon-Coles düzeltmesi ile.

Teori:
  Her takımın gol sayısı Poisson dağılımına uymaktadır.
  lambda_home = attack_h × defense_a × league_avg × home_advantage
  lambda_away = attack_a × defense_h × league_avg

Dixon-Coles (1997):
  Düşük skorlu maçlarda (0-0, 1-0, 0-1, 1-1) korelasyon düzeltmesi yapar.
"""

import numpy as np
from scipy.stats import poisson
from scipy.optimize import minimize
from typing import Optional
import logging

logger = logging.getLogger(__name__)

HOME_ADVANTAGE = 1.15   # İç saha katsayısı
MAX_GOALS      = 7      # Skor matrisinin boyutu
RHO            = -0.13  # Dixon-Coles korelasyon parametresi


class PoissonModel:

    def predict(
        self,
        home_attack: float,
        home_defense: float,
        away_attack: float,
        away_defense: float,
        league_avg_goals: float = 2.65,
    ) -> dict:
        """
        Verilen takım gücü metrikleriyle tahmin üretir.

        Args:
            home_attack:      Ev sahibinin ortalama gol/maç (iç saha)
            home_defense:     Ev sahibinin ortalama yenilen gol/maç (iç saha)
            away_attack:      Deplasman takımının ortalama gol/maç (deplasman)
            away_defense:     Deplasman takımının ortalama yenilen gol/maç (deplasman)
            league_avg_goals: Lig geneli ortalama gol/maç

        Returns:
            Tahmin sözlüğü
        """
        # Attack/Defense güç oranları (lig ortalamasına normalize)
        home_att_rate = home_attack  / (league_avg_goals / 2)
        away_def_rate = away_defense / (league_avg_goals / 2)
        away_att_rate = away_attack  / (league_avg_goals / 2)
        home_def_rate = home_defense / (league_avg_goals / 2)

        # Beklenen goller (Poisson lambda)
        lambda_home = home_att_rate * away_def_rate * (league_avg_goals / 2) * HOME_ADVANTAGE
        lambda_away = away_att_rate * home_def_rate * (league_avg_goals / 2)

        # Negatif olamaz
        lambda_home = max(lambda_home, 0.1)
        lambda_away = max(lambda_away, 0.1)

        # Skor olasılık matrisi (Dixon-Coles düzeltmeli)
        matrix = self._build_score_matrix(lambda_home, lambda_away)

        probs = self._extract_probabilities(matrix)
        score_matrix = self._build_score_dict(matrix)

        return {
            "lambda_home":           round(lambda_home, 3),
            "lambda_away":           round(lambda_away, 3),
            "home_win_prob":         round(probs["home_win"], 4),
            "draw_prob":             round(probs["draw"], 4),
            "away_win_prob":         round(probs["away_win"], 4),
            "over_2_5_prob":         round(probs["over_2_5"], 4),
            "btts_probability":      round(probs["btts"], 4),
            "predicted_home_goals":  round(lambda_home, 2),
            "predicted_away_goals":  round(lambda_away, 2),
            "score_matrix":          score_matrix,
        }

    def _build_score_matrix(self, lh: float, la: float) -> np.ndarray:
        """MAX_GOALS × MAX_GOALS boyutunda skor olasılık matrisi."""
        matrix = np.zeros((MAX_GOALS, MAX_GOALS))

        for h in range(MAX_GOALS):
            for a in range(MAX_GOALS):
                p_h = poisson.pmf(h, lh)
                p_a = poisson.pmf(a, la)
                dc  = self._dixon_coles_correction(h, a, lh, la)
                matrix[h][a] = p_h * p_a * dc

        # Toplam 1'e normalize et
        total = matrix.sum()
        if total > 0:
            matrix /= total

        return matrix

    def _dixon_coles_correction(self, h: int, a: int, lh: float, la: float) -> float:
        """
        Dixon-Coles (1997) düşük skor korelasyon düzeltmesi.
        Sadece (0,0), (1,0), (0,1), (1,1) skorlarına uygulanır.
        """
        if h == 0 and a == 0:
            return 1 - lh * la * RHO
        elif h == 1 and a == 0:
            return 1 + la * RHO
        elif h == 0 and a == 1:
            return 1 + lh * RHO
        elif h == 1 and a == 1:
            return 1 - RHO
        return 1.0

    def _extract_probabilities(self, matrix: np.ndarray) -> dict:
        """Skor matrisinden 1X2, over/under, BTTS olasılıklarını hesapla."""
        home_win = float(np.sum(np.tril(matrix, -1)))
        draw     = float(np.sum(np.diag(matrix)))
        away_win = float(np.sum(np.triu(matrix, 1)))

        # Over 2.5 = toplam gol >= 3 olasılığı
        over_2_5 = 0.0
        for h in range(MAX_GOALS):
            for a in range(MAX_GOALS):
                if h + a >= 3:
                    over_2_5 += matrix[h][a]

        # BTTS = her iki takım da gol atacak
        btts = 1.0 - (
            np.sum(matrix[0, :]) +   # ev sahibi 0 gol
            np.sum(matrix[:, 0]) -   # deplasman 0 gol
            matrix[0][0]             # çift sayım düzeltmesi
        )

        return {
            "home_win": home_win,
            "draw":     draw,
            "away_win": away_win,
            "over_2_5": over_2_5,
            "btts":     max(0.0, float(btts)),
        }

    def _build_score_dict(self, matrix: np.ndarray) -> dict:
        """Skor matrisini {skor: olasılık} dict'ine dönüştür."""
        scores = {}
        for h in range(MAX_GOALS):
            for a in range(MAX_GOALS):
                scores[f"{h}-{a}"] = round(float(matrix[h][a]), 4)
        return scores
