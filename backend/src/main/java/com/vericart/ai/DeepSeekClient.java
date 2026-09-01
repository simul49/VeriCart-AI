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
public class DeepSeekClient {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final AiMockService mockService;
    private final boolean keyIsPlaceholder;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DeepSeekClient(@Value("${ai.deepseek.api-key}") String apiKey,
                          @Value("${ai.deepseek.base-url}") String baseUrl,
                          @Value("${ai.deepseek.model}") String model,
                          AiMockService mockService) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.mockService = mockService;
        this.keyIsPlaceholder = apiKey == null || apiKey.startsWith("YOUR_") || apiKey.isBlank();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();

        if (keyIsPlaceholder && !mockService.isEnabled()) {
            log.error("DeepSeek API key is a placeholder and mock mode is OFF. AI calls will fail.");
        }
    }

    public Map<String, Object> detectFakeReview(String reviewContent, String productName) {
        if (mockService.isEnabled()) {
            log.debug("[DeepSeek Mock] Simulating fake review detection");
            return mockService.detectFakeReview(reviewContent, productName);
        }

        String systemPrompt = """
            You are a fake review detection expert. Analyze the following review for authenticity.
            Consider: repetitive content, generic praise, unnatural language, spam patterns, overly promotional tone.
            Return ONLY a JSON object with:
            {
              "probability": <0-100 number>,
              "isSuspicious": <true/false>,
              "reasons": ["reason1", "reason2"],
              "confidence": <0-100 number>
            }
            """;

        String response = callAPI(systemPrompt, "Review for product '" + productName + "': " + reviewContent);
        return parseJsonResponse(response);
    }

    public Map<String, Object> analyzeTrust(String productName, List<Map<String, Object>> reviews) {
        if (mockService.isEnabled()) {
            log.debug("[DeepSeek Mock] Simulating trust analysis");
            return mockService.analyzeTrust(productName, reviews);
        }

        String systemPrompt = """
            You are a trust analysis expert for e-commerce. Analyze the following reviews and determine a trust score.
            Consider: review consistency, review detail level, review authenticity patterns, sentiment consistency.
            Return ONLY a JSON object with:
            {
              "trustScore": <0-100 number>,
              "analysis": "brief analysis",
              "concerns": ["concern1", "concern2"],
              "strengths": ["strength1", "strength2"]
            }
            """;

        StringBuilder sb = new StringBuilder("Product: ").append(productName).append("\nReviews:\n");
        for (int i = 0; i < Math.min(reviews.size(), 10); i++) {
            Map<String, Object> r = reviews.get(i);
            sb.append("- Rating: ").append(r.get("rating")).append(", Review: ").append(r.get("content")).append("\n");
        }

        String response = callAPI(systemPrompt, sb.toString());
        return parseJsonResponse(response);
    }

    private String callAPI(String systemPrompt, String userMessage) {
        if (keyIsPlaceholder) {
            throw new RuntimeException("DeepSeek API key is not configured. Set DEEPSEEK_API_KEY env var or enable ai.mock.enabled=true");
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "temperature", 0.3,
                    "max_tokens", 1000
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode != 200) {
                String errorBody = response.body();
                log.error("DeepSeek API returned HTTP {}: {}", statusCode, errorBody);
                throw new RuntimeException(String.format("DeepSeek API error (HTTP %d): %s", statusCode,
                        errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody));
            }

            Map<String, Object> result = objectMapper.readValue(response.body(), new TypeReference<>() {});

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
            throw new RuntimeException("DeepSeek API returned no choices in response: " + response.body());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("DeepSeek API call failed: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> parseJsonResponse(String response) {
        try {
            String json = response;
            if (json.contains("```json")) {
                json = json.substring(json.indexOf("```json") + 7, json.lastIndexOf("```"));
            } else if (json.contains("```")) {
                json = json.substring(json.indexOf("```") + 3, json.lastIndexOf("```"));
            }
            json = json.trim();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON: {}", response);
            return Map.of("raw", response, "parseError", e.getMessage());
        }
    }
}
