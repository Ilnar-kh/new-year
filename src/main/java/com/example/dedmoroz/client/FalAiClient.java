package com.example.dedmoroz.client;

import com.example.dedmoroz.config.FalProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FalAiClient {
    private final WebClient webClient;
    private final FalProperties properties;

    public Mono<JobResponse> createJob(String text) {
        var payload = Map.of("model", properties.model(), "prompt", text);
        return webClient.post()
                .uri("https://api.fal.ai/v1/jobs")
                .header("Authorization", "Key " + properties.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(JobResponse.class);
    }

    public Mono<JobResponse> getJob(String jobId) {
        return webClient.get()
                .uri("https://api.fal.ai/v1/jobs/{id}", jobId)
                .header("Authorization", "Key " + properties.apiKey())
                .retrieve()
                .bodyToMono(JobResponse.class);
    }

    public record JobResponse(String id, String status, Map<String, Object> result) {}
}
