from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from app.routers import prediction, model
from app.database import engine
from app.config import settings


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: modelleri yükle
    from app.services.model_registry import ModelRegistry
    await ModelRegistry.load_all()
    yield
    # Shutdown


app = FastAPI(
    title="IbisScore AI Service",
    description="Futbol maç tahmini — Poisson + XGBoost ensemble",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(prediction.router, prefix="/predict", tags=["Predictions"])
app.include_router(model.router, prefix="/model", tags=["Model"])


@app.get("/health")
def health():
    return {"status": "ok", "service": "ibisscore-ai-service"}
