package com.ibisscore.ingestion.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * API-Football (RapidAPI) istemcisi.
 * Rate limit: 10 requests/minute (free tier).
 * Bucket4j ile throttle edilir.
 */
@Slf4j
@Component
public class ApiFootballClient {

    private final RestTemplate restTemplate;
    private final Bucket bucket;

    @Value("${api-football.key}")
    private String apiKey;

    @Value("${api-football.host}")
    private String apiHost;

    @Value("${api-football.base-url}")
    private String baseUrl;

    public ApiFootballClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        // 10 request/dakika — free tier limiti
        this.bucket = Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillIntervally(10, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    @Retryable(
        retryFor = Exception.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 5000, multiplier = 2.0)
    )
    public JsonNode get(String endpoint) {
        // Rate limit kontrolü — eğer token yoksa bekle
        try {
            bucket.asBlocking().consume(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Rate limiter interrupted", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", apiKey);
        headers.set("X-RapidAPI-Host", apiHost);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = baseUrl + endpoint;

        log.debug("API-Football GET: {}", url);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("API-Football returned: " + response.getStatusCode());
        }

        JsonNode body = response.getBody();
        if (body != null && body.has("errors")) {
            JsonNode errors = body.get("errors");
            if (!errors.isEmpty()) {
                log.error("API-Football errors: {}", errors);
                throw new RuntimeException("API-Football error: " + errors);
            }
        }

        return body;
    }

    public JsonNode getFixtures(String date) {
        return get("/fixtures?date=" + date);
    }

    public JsonNode getFixturesByLeague(int leagueId, int season) {
        return get("/fixtures?league=" + leagueId + "&season=" + season);
    }

    public JsonNode getFixtureStatistics(int fixtureId) {
        return get("/fixtures/statistics?fixture=" + fixtureId);
    }

    public JsonNode getOdds(int fixtureId) {
        return get("/odds?fixture=" + fixtureId + "&bookmaker=6"); // Bet365
    }

    public JsonNode getTeamStatistics(int teamId, int leagueId, int season) {
        return get("/teams/statistics?team=" + teamId + "&league=" + leagueId + "&season=" + season);
    }

    public JsonNode getStandings(int leagueId, int season) {
        return get("/standings?league=" + leagueId + "&season=" + season);
    }

    public JsonNode getHeadToHead(int team1Id, int team2Id) {
        return get("/fixtures/headtohead?h2h=" + team1Id + "-" + team2Id + "&last=10");
    }
}
