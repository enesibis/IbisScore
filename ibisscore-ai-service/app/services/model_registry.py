"""
Model Registry — uygulama başlangıcında modelleri yükler.
Singleton pattern.
"""

from app.models.poisson_model import PoissonModel
from app.models.xgboost_model import XGBoostModel
from app.services.ensemble import EnsemblePredictor
from app.config import settings
import logging

logger = logging.getLogger(__name__)


class ModelRegistry:
    _poisson:   PoissonModel    = None
    _xgboost:   XGBoostModel    = None
    _ensemble:  EnsemblePredictor = None

    @classmethod
    async def load_all(cls):
        logger.info("Loading ML models...")
        cls._poisson  = PoissonModel()
        cls._xgboost  = XGBoostModel(model_dir=settings.model_dir)
        cls._xgboost.load()  # Model dosyası yoksa sessizce devam eder
        cls._ensemble = EnsemblePredictor(cls._poisson, cls._xgboost)
        logger.info("Models ready. XGBoost loaded: %s", cls._xgboost._loaded)

    @classmethod
    def get_ensemble(cls) -> EnsemblePredictor:
        if cls._ensemble is None:
            raise RuntimeError("Models not loaded. Call load_all() first.")
        return cls._ensemble

    @classmethod
    def get_xgboost(cls) -> XGBoostModel:
        return cls._xgboost
