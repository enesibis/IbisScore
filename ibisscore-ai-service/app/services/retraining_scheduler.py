"""
Haftalık model yeniden eğitim scheduler.
APScheduler ile her Pazartesi 03:00'da çalışır.
"""

import logging
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from apscheduler.triggers.cron import CronTrigger

from app.database import SessionLocal
from app.routers.model import _run_training

logger = logging.getLogger(__name__)

_scheduler: AsyncIOScheduler | None = None


def start_scheduler() -> AsyncIOScheduler:
    global _scheduler
    _scheduler = AsyncIOScheduler(timezone="Europe/Istanbul")

    # Her Pazartesi 03:00
    _scheduler.add_job(
        _retrain_job,
        trigger=CronTrigger(day_of_week="mon", hour=3, minute=0),
        id="weekly_retrain",
        name="Haftalık XGBoost yeniden eğitimi",
        replace_existing=True,
        misfire_grace_time=3600,  # 1 saat gecikmeye tolerans
    )

    _scheduler.start()
    logger.info("Retraining scheduler started — runs every Monday 03:00 Istanbul time")
    return _scheduler


def stop_scheduler():
    global _scheduler
    if _scheduler and _scheduler.running:
        _scheduler.shutdown(wait=False)
        logger.info("Retraining scheduler stopped")


def _retrain_job():
    """Scheduler callback — sync wrapper for _run_training."""
    logger.info("Weekly retraining job triggered")
    db = SessionLocal()
    try:
        _run_training(db)
    except Exception as e:
        logger.error("Retraining job failed: %s", e, exc_info=True)
    finally:
        db.close()
