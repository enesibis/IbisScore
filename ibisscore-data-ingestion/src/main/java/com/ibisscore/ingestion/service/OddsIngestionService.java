package com.ibisscore.ingestion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibisscore.common.enums.MatchStatus;
import com.ibisscore.ingestion.client.ApiFootballClient;
import com.ibisscore.ingestion.entity.FixtureEntity;
import com.ibisscore.ingestion.entity.OddsEntity;
import com.ibisscore.ingestion.repository.FixtureEntityRepository;
import com.ibisscore.ingestion.repository.OddsEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OddsIngestionService {

    private final ApiFootballClient apiClient;
    private final FixtureEntityRepository fixtureRepository;
    private final OddsEntityRepository oddsRepository;

    @Transactional
    public void fetchOddsForUpcomingFixtures() {
        LocalDateTime now   = LocalDateTime.now();
        LocalDateTime limit = now.plusHours(48);

        List<FixtureEntity> upcoming = fixtureRepository
                .findByStatusAndMatchDateBetween(MatchStatus.NS, now, limit);

        log.info("Fetching odds for {} upcoming fixtures", upcoming.size());

        for (FixtureEntity fixture : upcoming) {
            try {
                fetchOddsForFixture(fixture);
                Thread.sleep(200); // Rate limit uyumu için küçük bekleme
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Odds fetch failed for fixture {}: {}", fixture.getApiId(), e.getMessage());
            }
        }
    }

    private void fetchOddsForFixture(FixtureEntity fixture) {
        JsonNode response = apiClient.getOdds(fixture.getApiId());
        if (response == null || !response.has("response")) return;

        JsonNode fixtureOdds = response.get("response");
        if (fixtureOdds.isEmpty()) return;

        JsonNode firstBookmaker = fixtureOdds.get(0).get("bookmakers");
        if (firstBookmaker == null || firstBookmaker.isEmpty()) return;

        for (JsonNode bookmakerNode : firstBookmaker) {
            String bookmakerName = bookmakerNode.get("name").asText();
            JsonNode bets = bookmakerNode.get("bets");

            OddsEntity odds = oddsRepository
                    .findByFixtureIdAndBookmaker(fixture.getId(), bookmakerName)
                    .orElse(new OddsEntity());

            odds.setFixture(fixture);
            odds.setBookmaker(bookmakerName);
            odds.setFetchedAt(LocalDateTime.now());

            for (JsonNode bet : bets) {
                String betName = bet.get("name").asText();
                JsonNode values = bet.get("values");

                switch (betName) {
                    case "Match Winner" -> parseMatchWinner(odds, values);
                    case "Goals Over/Under" -> parseGoalsOverUnder(odds, values);
                    case "Both Teams Score" -> parseBtts(odds, values);
                }
            }

            oddsRepository.save(odds);
        }
    }

    private void parseMatchWinner(OddsEntity odds, JsonNode values) {
        for (JsonNode v : values) {
            String value = v.get("value").asText();
            double odd   = v.get("odd").asDouble();
            switch (value) {
                case "Home" -> odds.setHomeWinOdd(odd);
                case "Draw" -> odds.setDrawOdd(odd);
                case "Away" -> odds.setAwayWinOdd(odd);
            }
        }
    }

    private void parseGoalsOverUnder(OddsEntity odds, JsonNode values) {
        for (JsonNode v : values) {
            String value = v.get("value").asText();
            double odd   = v.get("odd").asDouble();
            switch (value) {
                case "Over 2.5"  -> odds.setOver25Odd(odd);
                case "Under 2.5" -> odds.setUnder25Odd(odd);
            }
        }
    }

    private void parseBtts(OddsEntity odds, JsonNode values) {
        for (JsonNode v : values) {
            String value = v.get("value").asText();
            double odd   = v.get("odd").asDouble();
            if ("Yes".equals(value)) odds.setBttsYesOdd(odd);
            if ("No".equals(value))  odds.setBttsNoOdd(odd);
        }
    }
}
