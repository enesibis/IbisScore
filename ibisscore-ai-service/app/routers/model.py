from fastapi import APIRouter, HTTPException, Depends, BackgroundTasks
from sqlalchemy.orm import Session
from sqlalchemy import text

from app.schemas import ModelMetrics
from app.services.model_registry import ModelRegistry
from app.database import get_db
import pandas as pd
import logging

router = APIRouter()
logger = logging.getLogger(__name__)


@router.get("/metrics", response_model=ModelMetrics)
def get_model_metrics(db: Session = Depends(get_db)):
    """Model performans metriklerini döndürür."""
    xgb = ModelRegistry.get_xgboost()

    if not xgb._loaded:
        raise HTTPException(status_code=404, detail="Model henüz eğitilmemiş")

    # Validation set üzerinde son metrikler
    metrics = getattr(xgb, "_last_metrics", {})

    return ModelMetrics(
        model_name       = "XGBoost + Poisson Ensemble",
        accuracy         = metrics.get("accuracy", 0.0),
        precision_home   = metrics.get("precision_home", 0.0),
        precision_draw   = metrics.get("precision_draw", 0.0),
        precision_away   = metrics.get("precision_away", 0.0),
        recall_home      = metrics.get("recall_home", 0.0),
        recall_draw      = metrics.get("recall_draw", 0.0),
        recall_away      = metrics.get("recall_away", 0.0),
        log_loss         = metrics.get("log_loss", 0.0),
        brier_score      = metrics.get("brier_score", 0.0),
        calibration_error= metrics.get("calibration_error", 0.0),
        sample_count     = metrics.get("sample_count", 0),
        last_trained     = metrics.get("last_trained"),
    )


@router.post("/retrain")
async def retrain_model(
    background_tasks: BackgroundTasks,
    db: Session = Depends(get_db)
):
    """
    Modeli arka planda yeniden eğitir.
    Tamamlanan maçların sonuçlarını kullanır.
    """
    background_tasks.add_task(_run_training, db)
    return {"message": "Model eğitimi başlatıldı. /model/metrics ile takip edebilirsiniz."}


def _run_training(db: Session):
    """
    Veritabanındaki tamamlanmış maçlardan training dataset oluştur ve eğit.
    """
    logger.info("Starting model retraining...")

    rows = db.execute(text("""
        SELECT
            f.id                                    AS fixture_id,
            -- Etiket
            CASE
                WHEN f.home_goals > f.away_goals THEN 0
                WHEN f.home_goals = f.away_goals THEN 1
                ELSE 2
            END                                     AS result,
            -- Ev sahibi stats
            hs.avg_goals_for_home,
            hs.avg_goals_against_home,
            hs.form_points                          AS home_form_points,
            CASE WHEN hs.played_home > 0
                 THEN hs.wins_home::float / hs.played_home ELSE 0.4 END AS home_home_win_rate,
            hs.league_position                      AS home_league_pos,
            -- Deplasman stats
            as2.avg_goals_for_away,
            as2.avg_goals_against_away,
            as2.form_points                         AS away_form_points,
            CASE WHEN as2.played_away > 0
                 THEN as2.wins_away::float / as2.played_away ELSE 0.4 END AS away_away_win_rate,
            as2.league_position                     AS away_league_pos,
            -- Oranlar
            o.home_win_odd,
            o.draw_odd,
            o.away_win_odd
        FROM fixtures f
        JOIN team_season_stats hs
            ON hs.team_id = f.home_team_id AND hs.league_id = f.league_id AND hs.season = 2024
        JOIN team_season_stats as2
            ON as2.team_id = f.away_team_id AND as2.league_id = f.league_id AND as2.season = 2024
        LEFT JOIN odds o ON o.fixture_id = f.id
        WHERE f.status = 'FT'
          AND f.home_goals IS NOT NULL
          AND f.away_goals IS NOT NULL
        ORDER BY f.match_date
    """)).fetchall()

    if len(rows) < 100:
        logger.warning("Yetersiz eğitim verisi: %d maç (min 100)", len(rows))
        return

    df = pd.DataFrame([dict(r._mapping) for r in rows])

    # Feature engineering
    df["attack_diff"]       = df["avg_goals_for_home"]    - df["avg_goals_for_away"]
    df["defense_diff"]      = df["avg_goals_against_away"] - df["avg_goals_against_home"]
    df["implied_prob_home"] = df["home_win_odd"].apply(lambda x: 1/x if x and x > 0 else 0.33)
    df["implied_prob_draw"] = df["draw_odd"].apply(lambda x: 1/x if x and x > 0 else 0.33)
    df["implied_prob_away"] = df["away_win_odd"].apply(lambda x: 1/x if x and x > 0 else 0.33)
    df["league_position_diff"] = df["home_league_pos"] - df["away_league_pos"]

    # Eksik H2H + dinlenme süresi için ortalama değerler
    df["h2h_home_wins"]  = 2
    df["h2h_draws"]      = 1
    df["h2h_away_wins"]  = 2
    df["h2h_avg_goals"]  = 2.5
    df["days_rest_home"] = 7
    df["days_rest_away"] = 7

    xgb = ModelRegistry.get_xgboost()
    metrics = xgb.train(df)
    logger.info("Retraining complete: %s", metrics)
