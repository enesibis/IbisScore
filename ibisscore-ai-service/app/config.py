from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str = "postgresql://ibisscore_user:change_me@localhost:5432/ibisscore"
    redis_url: str = "redis://localhost:6379/0"
    model_dir: str = "models/saved"

    # Ensemble ağırlıkları
    poisson_weight: float = 0.40
    xgboost_weight: float = 0.60

    # Value bet eşikleri
    min_ev_threshold: float = 0.05
    min_edge_threshold: float = 0.03

    class Config:
        env_file = ".env"


settings = Settings()
