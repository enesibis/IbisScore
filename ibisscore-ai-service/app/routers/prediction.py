from fastapi import APIRouter, HTTPException, Depends
from sqlalchemy.orm import Session

from app.schemas import PredictionResponse, BatchPredictionRequest, ScoreMatrix
from app.services.feature_builder import FeatureBuilder
from app.services.model_registry import ModelRegistry
from app.database import get_db

router = APIRouter()
feature_builder = FeatureBuilder()


@router.post("/match/{fixture_id}", response_model=PredictionResponse)
def predict_match(fixture_id: int, db: Session = Depends(get_db)):
    """
    Tek maç tahmini.
    Feature'ları DB'den çeker, Poisson+XGBoost ensemble çalıştırır.
    """
    features = feature_builder.build(fixture_id, db)
    if not features:
        raise HTTPException(
            status_code=404,
            detail=f"Maç için yeterli veri bulunamadı: fixture_id={fixture_id}"
        )

    ensemble = ModelRegistry.get_ensemble()
    result   = ensemble.predict(features)

    # Skor matrisinden top 5 skoru çıkar
    score_matrix = None
    if result.get("score_matrix"):
        raw = result["score_matrix"]
        top5 = sorted(raw.items(), key=lambda x: x[1], reverse=True)[:5]
        score_matrix = ScoreMatrix(
            probabilities=raw,
            most_likely_score=top5[0][0],
            top_scores=[{"score": k, "prob": v} for k, v in top5],
        )

    return PredictionResponse(
        fixture_id            = fixture_id,
        model_version         = result["model_version"],
        home_win_prob         = result["home_win_prob"],
        draw_prob             = result["draw_prob"],
        away_win_prob         = result["away_win_prob"],
        predicted_home_goals  = result["predicted_home_goals"],
        predicted_away_goals  = result["predicted_away_goals"],
        over_2_5_prob         = result["over_2_5_prob"],
        btts_probability      = result["btts_probability"],
        confidence_score      = result["confidence_score"],
        recommendation        = result["recommendation"],
        score_matrix          = score_matrix,
    )


@router.post("/batch", response_model=list[PredictionResponse])
def predict_batch(request: BatchPredictionRequest, db: Session = Depends(get_db)):
    """Toplu maç tahmini (max 20 maç)."""
    if len(request.fixture_ids) > 20:
        raise HTTPException(status_code=400, detail="Tek seferde maksimum 20 maç tahmin edilebilir")

    results = []
    for fid in request.fixture_ids:
        try:
            result = predict_match(fid, db)
            results.append(result)
        except HTTPException:
            continue  # Veri eksik olan maçı atla

    return results


@router.get("/value-bets")
def get_value_bets(db: Session = Depends(get_db)):
    """
    Bugünün maçları için value bet analizi.
    Yüksek EV'li maçları döndürür.
    """
    from sqlalchemy import text

    rows = db.execute(text("""
        SELECT id FROM fixtures
        WHERE DATE(match_date) = CURRENT_DATE
          AND status = 'NS'
        ORDER BY match_date
    """)).fetchall()

    value_bets = []
    ensemble   = ModelRegistry.get_ensemble()

    for row in rows:
        fid      = row[0]
        features = feature_builder.build(fid, db)
        if not features:
            continue

        result   = ensemble.predict(features)
        max_prob = max(result["home_win_prob"], result["draw_prob"], result["away_win_prob"])

        # Sadece güven skoru yüksek olanları döndür
        if result["confidence_score"] >= 0.65 and result["recommendation"] != "NO_BET":
            value_bets.append({
                "fixture_id":    fid,
                "recommendation": result["recommendation"],
                "confidence":    result["confidence_score"],
                "home_win_prob": result["home_win_prob"],
                "draw_prob":     result["draw_prob"],
                "away_win_prob": result["away_win_prob"],
            })

    # Güvene göre sırala
    value_bets.sort(key=lambda x: x["confidence"], reverse=True)
    return {"count": len(value_bets), "value_bets": value_bets}
