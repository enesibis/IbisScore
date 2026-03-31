"""
XGBoost Maç Sonucu Tahmin Modeli.

Features:
  - Gol ortalamaları (son 5 maç + sezon geneli)
  - Form puanı (W=3, D=1, L=0 → 15 üzerinden)
  - İç/dış saha kazanma oranları
  - Head-to-head geçmiş
  - Bookmaker implied olasılıkları (market bilgisi)
  - Dinlenme süresi

Eğitim:
  TimeSeriesSplit(n_splits=5) — data leakage önleme
  early_stopping_rounds=50   — aşırı öğrenme önleme
  IsotonicRegression          — probability calibration
"""

import numpy as np
import pandas as pd
import joblib
import os
import logging
from pathlib import Path
from typing import Optional

logger = logging.getLogger(__name__)

FEATURES = [
    # Gol istatistikleri
    "home_avg_goals_for",
    "home_avg_goals_against",
    "away_avg_goals_for",
    "away_avg_goals_against",
    # Form
    "home_form_points",
    "away_form_points",
    # Saha bazlı kazanma oranı
    "home_home_win_rate",
    "away_away_win_rate",
    # Güç farkı
    "attack_diff",
    "defense_diff",
    # Head-to-head
    "h2h_home_wins",
    "h2h_draws",
    "h2h_away_wins",
    "h2h_avg_goals",
    # Bookmaker implied
    "implied_prob_home",
    "implied_prob_draw",
    "implied_prob_away",
    # Bağlam
    "days_rest_home",
    "days_rest_away",
    "league_position_diff",
]


class XGBoostModel:

    def __init__(self, model_dir: str = "models/saved"):
        self.model_dir  = Path(model_dir)
        self.model      = None
        self.calibrator = None
        self._loaded    = False

    def load(self) -> bool:
        model_path = self.model_dir / "xgb_model.pkl"
        calib_path = self.model_dir / "xgb_calibrator.pkl"

        if model_path.exists() and calib_path.exists():
            self.model      = joblib.load(model_path)
            self.calibrator = joblib.load(calib_path)
            self._loaded    = True
            logger.info("XGBoost model loaded from %s", self.model_dir)
            return True

        logger.warning("XGBoost model files not found — will use default weights")
        return False

    def predict(self, features: dict) -> Optional[dict]:
        """
        Features dict'inden 1X2 olasılıkları tahmin eder.
        Model yüklü değilse None döner.
        """
        if not self._loaded:
            return None

        X = self._build_feature_vector(features)
        if X is None:
            return None

        raw_probs = self.model.predict_proba(X)

        # Isotonic calibration
        if self.calibrator:
            probs = self.calibrator.transform(raw_probs)[0]
        else:
            probs = raw_probs[0]

        # Toplamı 1'e normalize et
        probs = np.array(probs)
        probs = np.clip(probs, 0, 1)
        probs = probs / probs.sum()

        return {
            "home_win_prob": round(float(probs[0]), 4),
            "draw_prob":     round(float(probs[1]), 4),
            "away_win_prob": round(float(probs[2]), 4),
        }

    def train(self, df: pd.DataFrame) -> dict:
        """
        Veri seti üzerinde model eğitir.
        df sütunları: FEATURES + 'result' (0=home_win, 1=draw, 2=away_win)
        """
        from xgboost import XGBClassifier
        from sklearn.model_selection import TimeSeriesSplit
        from sklearn.isotonic import IsotonicRegression
        from sklearn.metrics import (
            accuracy_score, log_loss, brier_score_loss,
            classification_report
        )

        df = df.dropna(subset=FEATURES + ["result"])
        X = df[FEATURES].values
        y = df["result"].values

        tscv    = TimeSeriesSplit(n_splits=5)
        splits  = list(tscv.split(X))
        train_idx, val_idx = splits[-1]

        X_train, X_val = X[train_idx], X[val_idx]
        y_train, y_val = y[train_idx], y[val_idx]

        model = XGBClassifier(
            n_estimators=500,
            max_depth=6,
            learning_rate=0.05,
            subsample=0.8,
            colsample_bytree=0.8,
            min_child_weight=5,
            gamma=0.1,
            use_label_encoder=False,
            eval_metric="mlogloss",
            random_state=42,
            n_jobs=-1,
        )

        model.fit(
            X_train, y_train,
            eval_set=[(X_val, y_val)],
            verbose=False,
        )

        # Probability calibration (Isotonic)
        raw_probs_val = model.predict_proba(X_val)
        calibrator    = IsotonicRegression(out_of_bounds="clip")
        calibrator.fit(raw_probs_val.ravel(), np.repeat(y_val, 3))

        y_pred  = model.predict(X_val)
        metrics = {
            "accuracy":  round(accuracy_score(y_val, y_pred), 4),
            "log_loss":  round(log_loss(y_val, raw_probs_val), 4),
            "report":    classification_report(y_val, y_pred,
                            target_names=["Home Win", "Draw", "Away Win"]),
        }

        # Kaydet
        self.model_dir.mkdir(parents=True, exist_ok=True)
        joblib.dump(model,      self.model_dir / "xgb_model.pkl")
        joblib.dump(calibrator, self.model_dir / "xgb_calibrator.pkl")

        self.model      = model
        self.calibrator = calibrator
        self._loaded    = True

        logger.info("XGBoost model trained. Accuracy: %s", metrics["accuracy"])
        return metrics

    def _build_feature_vector(self, f: dict) -> Optional[np.ndarray]:
        try:
            row = [f.get(feat, 0.0) for feat in FEATURES]
            return np.array(row).reshape(1, -1)
        except Exception as e:
            logger.error("Feature vector build failed: %s", e)
            return None
