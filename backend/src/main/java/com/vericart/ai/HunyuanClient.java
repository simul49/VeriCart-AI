package com.vericart.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HunyuanClient {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HunyuanClient(@Value("${ai.hunyuan.api-key}") String apiKey,
                         @Value("${ai.hunyuan.base-url}") String baseUrl,
                         @Value("${ai.hunyuan.model}") String model) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> generateReviewSummary(List<Map<String, Object>> reviews, String productName) {
        String systemPrompt = """
            You are an expert review summarizer for e-commerce. Analyze customer reviews and produce a summary.
            Return ONLY a JSON:
            {
              "advantages": ["adv1", "adv2", "adv3"],
              "disadvantages": ["dis1", "dis2", "dis3"],
              "overallOpinion": "summary in one sentence",
              "recommendation": "BUY/CONSIDER/AVOID",
              "recommendationReason": "why"
            }
            """;

        StringBuilder sb = new StringBuilder("Product: ").append(productName).append("\nReviews:\n");
        for (int i = 0; i < Math.min(reviews.size(), 20); i++) {
            Map<String, Object> r = reviews.get(i);
            sb.append("- Rating: ").append(r.get("rating")).append("/5");
            if (r.get("content") != null && !r.get("content").toString().isBlank()) {
                sb.append(", ").append(r.get("content"));
            }
            sb.append("\n");
        }

        String response = callAPI(systemPrompt, sb.toString());
        return parseJson(response);
    }

    private String callAPI(String systemPrompt, String userMessage) {
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "temperature", 0.4,
                    "max_tokens", 1000
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
            return "{}";
        } catch (Exception e) {
            log.error("Hunyuan API call failed", e);
            return "{}";
        }
    }

    private Map<String, Object> parseJson(String response) {
        try {
            String json = response;
            if (json.contains("```json")) {
                json = json.substring(json.indexOf("```json") + 7, json.lastIndexOf("```"));
            } else if (json.contains("```")) {
                json = json.substring(json.indexOf("```") + 3, json.lastIndexOf("```"));
            }
            return objectMapper.readValue(json.trim(), new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse Hunyuan JSON: {}", response);
            return Map.of("raw", response);
        }
    }
}
