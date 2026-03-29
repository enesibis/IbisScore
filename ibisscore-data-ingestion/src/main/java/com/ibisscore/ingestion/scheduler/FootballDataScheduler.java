package com.ibisscore.ingestion.scheduler;

import com.ibisscore.ingestion.service.FixtureIngestionService;
import com.ibisscore.ingestion.service.OddsIngestionService;
import com.ibisscore.ingestion.service.TeamStatsIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class FootballDataScheduler {

    private final FixtureIngestionService fixtureService;
    private final OddsIngestionService oddsService;
    private final TeamStatsIngestionService teamStatsService;

    /**
     * Her gün sabah 06:00 — Günlük fikstürleri çek (bugün + yarın)
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void fetchDailyFixtures() {
        log.info("[CRON] Daily fixtures fetch started: {}", LocalDateTime.now());
        try {
            fixtureService.fetchAndSaveFixtures(LocalDate.now());
            fixtureService.fetchAndSaveFixtures(LocalDate.now().plusDays(1));
        } catch (Exception e) {
            log.error("[CRON] Daily fixtures fetch failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Her 2 saatte bir — Yaklaşan maçların oranlarını güncelle
     */
    @Scheduled(cron = "0 0 */2 * * *")
    public void fetchUpcomingOdds() {
        log.info("[CRON] Odds fetch started: {}", LocalDateTime.now());
        try {
            oddsService.fetchOddsForUpcomingFixtures();
        } catch (Exception e) {
            log.error("[CRON] Odds fetch failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Her 5 dakika — Canlı maç skorları (maç günlerinde aktif)
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void fetchLiveScores() {
        try {
            fixtureService.fetchAndUpdateLiveFixtures();
        } catch (Exception e) {
            log.error("[CRON] Live scores fetch failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Her Pazartesi 03:00 — Takım sezon istatistiklerini güncelle
     */
    @Scheduled(cron = "0 0 3 * * MON")
    public void fetchTeamStats() {
        log.info("[CRON] Weekly team stats fetch started: {}", LocalDateTime.now());
        try {
            teamStatsService.fetchAndUpdateAllTeamStats(2024);
        } catch (Exception e) {
            log.error("[CRON] Team stats fetch failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Her gece 02:00 — Dün tamamlanan maçların sonuçlarını kaydet
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void fetchYesterdayResults() {
        log.info("[CRON] Yesterday results fetch: {}", LocalDate.now().minusDays(1));
        try {
            fixtureService.fetchAndSaveFixtures(LocalDate.now().minusDays(1));
        } catch (Exception e) {
            log.error("[CRON] Yesterday results fetch failed: {}", e.getMessage(), e);
        }
    }
}
