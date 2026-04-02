package com.ibisscore.ingestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibisscore.common.enums.MatchStatus;
import com.ibisscore.common.event.FixtureEvent;
import com.ibisscore.ingestion.client.ApiFootballClient;
import com.ibisscore.ingestion.entity.FixtureEntity;
import com.ibisscore.ingestion.entity.LeagueEntity;
import com.ibisscore.ingestion.entity.TeamEntity;
import com.ibisscore.ingestion.repository.FixtureEntityRepository;
import com.ibisscore.ingestion.repository.LeagueEntityRepository;
import com.ibisscore.ingestion.repository.TeamEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixtureIngestionService {

    // Takip edilen ligler (API-Football ID'leri)
    private static final Set<Integer> TRACKED_LEAGUE_IDS = Set.of(
            203, // Süper Lig
            39,  // Premier League
            140, // La Liga
            135, // Serie A
            78,  // Bundesliga
            61,  // Ligue 1
            2,   // Champions League
            3    // Europa League
    );

    private final ApiFootballClient apiClient;
    private final FixtureEntityRepository fixtureRepository;
    private final LeagueEntityRepository leagueRepository;
    private final TeamEntityRepository teamRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void fetchAndSaveFixtures(LocalDate date) {
        log.info("Fetching fixtures for date: {}", date);
        JsonNode response = apiClient.getFixtures(date.toString());

        if (response == null || !response.has("response")) {
            log.warn("No fixtures data for date: {}", date);
            return;
        }

        int savedCount = 0;
        for (JsonNode fixtureNode : response.get("response")) {
            try {
                processFixtureNode(fixtureNode, false);
                savedCount++;
            } catch (Exception e) {
                log.error("Failed to process fixture: {}", e.getMessage());
            }
        }
        log.info("Saved {} fixtures for {}", savedCount, date);
    }

    @Transactional
    public void fetchAndUpdateLiveFixtures() {
        JsonNode response = apiClient.get("/fixtures?live=all");
        if (response == null || !response.has("response")) return;

        for (JsonNode node : response.get("response")) {
            try {
                processFixtureNode(node, true);
            } catch (Exception e) {
                log.error("Failed to update live fixture: {}", e.getMessage());
            }
        }
    }

    private void processFixtureNode(JsonNode node, boolean isLive) {
        JsonNode fixture  = node.get("fixture");
        JsonNode league   = node.get("league");
        JsonNode teams    = node.get("teams");
        JsonNode goals    = node.get("goals");
        JsonNode score    = node.get("score");

        int leagueApiId = league.get("id").asInt();
        // TODO: production'da sadece TRACKED_LEAGUE_IDS filtrele
        // if (!TRACKED_LEAGUE_IDS.contains(leagueApiId)) return;

        int fixtureApiId  = fixture.get("id").asInt();
        String dateStr    = fixture.get("date").asText();
        String statusStr  = fixture.get("status").get("short").asText();

        // Takımları kaydet/güncelle
        TeamEntity homeTeam = saveOrUpdateTeam(teams.get("home"));
        TeamEntity awayTeam = saveOrUpdateTeam(teams.get("away"));

        // Ligi kaydet/güncelle
        LeagueEntity leagueEntity = saveOrUpdateLeague(league);

        // Fixture kaydet/güncelle
        FixtureEntity entity = fixtureRepository.findByApiId(fixtureApiId)
                .orElse(new FixtureEntity());

        boolean isNew = entity.getId() == null;

        entity.setApiId(fixtureApiId);
        entity.setLeague(leagueEntity);
        entity.setHomeTeam(homeTeam);
        entity.setAwayTeam(awayTeam);
        entity.setMatchDate(ZonedDateTime.parse(dateStr).toLocalDateTime());
        entity.setStatus(parseStatus(statusStr));
        entity.setHomeGoals(goals.get("home").isNull() ? null : goals.get("home").asInt());
        entity.setAwayGoals(goals.get("away").isNull() ? null : goals.get("away").asInt());

        if (score.has("halftime")) {
            JsonNode ht = score.get("halftime");
            entity.setHomeGoalsHt(ht.get("home").isNull() ? null : ht.get("home").asInt());
            entity.setAwayGoalsHt(ht.get("away").isNull() ? null : ht.get("away").asInt());
        }

        fixtureRepository.save(entity);

        // RabbitMQ'ya event gönder
        FixtureEvent.EventType eventType = isNew
                ? FixtureEvent.EventType.FIXTURE_CREATED
                : FixtureEvent.EventType.FIXTURE_UPDATED;

        if (isNew || isLive) {
            FixtureEvent event = FixtureEvent.builder()
                    .type(eventType)
                    .fixtureId(entity.getId())
                    .apiId(fixtureApiId)
                    .occurredAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend("ibisscore.fixtures", "fixture.event", event);
        }
    }

    private TeamEntity saveOrUpdateTeam(JsonNode teamNode) {
        int apiId = teamNode.get("id").asInt();
        return teamRepository.findByApiId(apiId).orElseGet(() -> {
            TeamEntity team = new TeamEntity();
            team.setApiId(apiId);
            team.setName(teamNode.get("name").asText());
            team.setLogoUrl(teamNode.has("logo") ? teamNode.get("logo").asText() : null);
            return teamRepository.save(team);
        });
    }

    private LeagueEntity saveOrUpdateLeague(JsonNode leagueNode) {
        int apiId = leagueNode.get("id").asInt();
        return leagueRepository.findByApiId(apiId).orElseGet(() -> {
            LeagueEntity league = new LeagueEntity();
            league.setApiId(apiId);
            league.setName(leagueNode.get("name").asText());
            league.setCountry(leagueNode.get("country").asText());
            league.setSeason(leagueNode.get("season").asInt());
            league.setLogoUrl(leagueNode.has("logo") ? leagueNode.get("logo").asText() : null);
            return leagueRepository.save(league);
        });
    }

    private MatchStatus parseStatus(String statusCode) {
        try {
            return MatchStatus.valueOf(statusCode);
        } catch (IllegalArgumentException e) {
            return MatchStatus.NS;
        }
    }
}
