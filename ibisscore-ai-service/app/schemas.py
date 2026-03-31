from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class PredictionRequest(BaseModel):
    fixture_id: int


class BatchPredictionRequest(BaseModel):
    fixture_ids: list[int]


class ScoreMatrix(BaseModel):
    """0-0'dan 6-6'ya skor olasılık matrisi"""
    probabilities: dict[str, float]  # "0-0": 0.082, "1-0": 0.162, ...
    most_likely_score: str           # "1-0"
    top_scores: list[dict]           # [{"score": "1-0", "prob": 0.162}, ...]


class PredictionResponse(BaseModel):
    fixture_id: int
    model_version: str

    # 1X2
    home_win_prob: float = Field(ge=0, le=1)
    draw_prob: float      = Field(ge=0, le=1)
    away_win_prob: float  = Field(ge=0, le=1)

    # Beklenen goller
    predicted_home_goals: float
    predicted_away_goals: float

    # Ek marketler
    over_2_5_prob: float
    btts_probability: float

    # Model güveni
    confidence_score: float = Field(ge=0, le=1)

    # Öneri
    recommendation: str  # HOME_WIN, DRAW, AWAY_WIN, NO_BET

    # Skor matrisi (opsiyonel)
    score_matrix: Optional[ScoreMatrix] = None

    created_at: datetime = Field(default_factory=datetime.now)


class ModelMetrics(BaseModel):
    model_name: str
    accuracy: float
    precision_home: float
    precision_draw: float
    precision_away: float
    recall_home: float
    recall_draw: float
    recall_away: float
    log_loss: float
    brier_score: float
    calibration_error: float
    sample_count: int
    last_trained: Optional[datetime]
