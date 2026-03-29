package com.ibisscore.match.client;

import com.ibisscore.common.dto.PredictionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceClient {

    private final RestTemplate restTemplate;

    @Value("${ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public PredictionDTO predict(Long fixtureId) {
        try {
            String url = aiServiceUrl + "/predict/match/" + fixtureId;
            return restTemplate.postForObject(url, null, PredictionDTO.class);
        } catch (Exception e) {
            log.error("AI service call failed for fixture {}: {}", fixtureId, e.getMessage());
            throw new RuntimeException("AI servisi şu an yanıt vermiyor", e);
        }
    }
}
