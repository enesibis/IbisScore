package com.ibisscore.ingestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibisscore.ingestion.client.ApiFootballClient;
import com.ibisscore.ingestion.entity.TeamEntity;
import com.ibisscore.ingestion.entity.TeamSeasonStatsEntity;
import com.ibisscore.ingestion.repository.LeagueEntityRepository;
import com.ibisscore.ingestion.repository.TeamEntityRepository;
import com.ibisscore.ingestion.repository.TeamSeasonStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamStatsIngestionService {

    // Takip edilen ligler (apiId → season çiftleri)
    private static final Map<Integer, Integer> LEAGUES = Map.of(
            203, 2024,
            39,  2024,
            140, 2024,
            135, 2024,
            78,  2024,
            61,  2024
    );

    private final ApiFootballClient apiClient;
    private final TeamEntityRepository teamRepository;
    private final LeagueEntityRepository leagueRepository;
    private final TeamSeasonStatsRepository statsRepository;

    @Transactional
    public void fetchAndUpdateAllTeamStats(int season) {
        LEAGUES.forEach((leagueApiId, leagueSeason) -> {
            try {
                fetchStandingsAndUpdateStats(leagueApiId, leagueSeason);
            } catch (Exception e) {
                log.error("Stats fetch failed for league {}: {}", leagueApiId, e.getMessage());
            }
        });
    }

    private void fetchStandingsAndUpdateStats(int leagueApiId, int season) {
        JsonNode response = apiClient.getStandings(leagueApiId, season);
        if (response == null || !response.has("response")) return;

        JsonNode standings = response.get("response").get(0)
                .get("league").get("standings").get(0);

        for (JsonNode standing : standings) {
            try {
                updateTeamStats(standing, leagueApiId, season);
            } catch (Exception e) {
                log.error("Failed to update team stats: {}", e.getMessage());
            }
        }
        log.info("Team stats updated for league {} season {}", leagueApiId, season);
    }

    private void updateTeamStats(JsonNode standing, int leagueApiId, int season) {
        int teamApiId = standing.get("team").get("id").asInt();
        TeamEntity team = teamRepository.findByApiId(teamApiId).orElse(null);
        if (team == null) return;

        var leagueOpt = leagueRepository.findByApiId(leagueApiId);
        if (leagueOpt.isEmpty()) return;

        TeamSeasonStatsEntity stats = statsRepository
                .findByTeamIdAndLeagueIdAndSeason(team.getId(), leagueOpt.get().getId(), season)
                .orElse(new TeamSeasonStatsEntity());

        stats.setTeam(team);
        stats.setLeague(leagueOpt.get());
        stats.setSeason(season);
        stats.setLeaguePosition(standing.get("rank").asInt());
        stats.setPoints(standing.get("points").asInt());

        // Ev - Deplasman istatistikleri
        JsonNode home = standing.get("home");
        JsonNode away = standing.get("away");

        stats.setPlayedHome(home.get("played").asInt());
        stats.setPlayedAway(away.get("played").asInt());
        stats.setWinsHome(home.get("win").asInt());
        stats.setWinsAway(away.get("win").asInt());
        stats.setDrawsHome(home.get("draw").asInt());
        stats.setDrawsAway(away.get("draw").asInt());
        stats.setLossesHome(home.get("lose").asInt());
        stats.setLossesAway(away.get("lose").asInt());
        stats.setGoalsForHome(home.get("goals").get("for").asInt());
        stats.setGoalsForAway(away.get("goals").get("for").asInt());
        stats.setGoalsAgainstHome(home.get("goals").get("against").asInt());
        stats.setGoalsAgainstAway(away.get("goals").get("against").asInt());

        // Poisson lambda hesabı
        int homePlayed = Math.max(stats.getPlayedHome(), 1);
        int awayPlayed = Math.max(stats.getPlayedAway(), 1);
        stats.setAvgGoalsForHome((double) stats.getGoalsForHome() / homePlayed);
        stats.setAvgGoalsForAway((double) stats.getGoalsForAway() / awayPlayed);
        stats.setAvgGoalsAgainstHome((double) stats.getGoalsAgainstHome() / homePlayed);
        stats.setAvgGoalsAgainstAway((double) stats.getGoalsAgainstAway() / awayPlayed);

        // Form
        String form = standing.has("form") ? standing.get("form").asText() : "";
        stats.setForm(form.length() > 5 ? form.substring(form.length() - 5) : form);
        stats.setFormPoints(calculateFormPoints(stats.getForm()));

        statsRepository.save(stats);
    }

    private int calculateFormPoints(String form) {
        if (form == null) return 0;
        int points = 0;
        for (char c : form.toCharArray()) {
            if (c == 'W') points += 3;
            else if (c == 'D') points += 1;
        }
        return points;
    }
}
