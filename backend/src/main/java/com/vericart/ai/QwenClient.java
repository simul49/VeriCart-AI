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
public class QwenClient {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public QwenClient(@Value("${ai.qwen.api-key}") String apiKey,
                      @Value("${ai.qwen.base-url}") String baseUrl,
                      @Value("${ai.qwen.model}") String model) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> analyzeSentiment(String reviewContent) {
        String systemPrompt = """
            Analyze customer review sentiment. Return ONLY a JSON:
            {
              "sentiment": "POSITIVE/NEUTRAL/NEGATIVE",
              "emotion": "happy/satisfied/neutral/disappointed/angry",
              "score": <1-10>,
              "keyPoints": ["point1", "point2"]
            }
            """;

        String response = callAPI(systemPrompt, reviewContent);
        return parseJson(response);
    }

    public Map<String, Object> analyzeSentimentBatch(List<Map<String, Object>> reviews) {
        if (reviews.isEmpty()) return Map.of("positiveRatio", 50);

        int positive = 0, negative = 0, total = 0;
        for (Map<String, Object> r : reviews) {
            Object rating = r.get("rating");
            if (rating instanceof Number) {
                int rv = ((Number) rating).intValue();
                if (rv >= 4) positive++;
                else if (rv <= 2) negative++;
                total++;
            }
        }
        double ratio = total > 0 ? (positive * 100.0 / total) : 50;
        return Map.of("positiveRatio", ratio, "total", total, "positiveCount", positive, "negativeCount", negative);
    }

    public String chat(String productName, String question, List<Map<String, Object>> reviews) {
        String systemPrompt = """
            You are VeriCart AI Shopping Assistant. Help customers make informed purchasing decisions.
            Be honest, transparent, and base your answers on actual review data provided.
            If reviews indicate problems, mention them. If the product seems trustworthy, say so.
            Keep answers concise (2-4 sentences).
            """;

        StringBuilder sb = new StringBuilder();
        sb.append("Product: ").append(productName).append("\n");
        sb.append("Customer Question: ").append(question).append("\n");
        sb.append("Available Reviews:\n");
        for (int i = 0; i < Math.min(reviews.size(), 15); i++) {
            Map<String, Object> r = reviews.get(i);
            sb.append("- Rating ").append(r.get("rating")).append("/5: ").append(r.get("content")).append("\n");
        }

        return callAPI(systemPrompt, sb.toString());
    }

    public String generalChat(String question, List<Map<String, Object>> productContext) {
        String systemPrompt = """
            You are VeriCart AI Shopping Assistant. Help customers with general shopping questions,
            product comparisons, and purchasing advice. You have access to the store's product catalog.
            Be helpful, honest, and transparent. If you recommend a product, explain why.
            Keep answers friendly and concise (3-5 sentences).
            """;

        StringBuilder sb = new StringBuilder("Customer Question: ").append(question).append("\n\n");
        sb.append("Available Products in Store:\n");
        for (Map<String, Object> p : productContext) {
            sb.append("- ").append(p.get("name"))
              .append(" | $").append(p.get("price"))
              .append(" | Rating: ").append(p.get("rating"))
              .append("/5 | Trust Score: ").append(p.get("trustScore") != null ? p.get("trustScore") : "N/A")
              .append("\n");
        }

        return callAPI(systemPrompt, sb.toString());
    }

    public String recommend(String userPreferences, List<Map<String, Object>> products) {
        String systemPrompt = """
            You are a product recommendation expert. Based on user preferences and available products,
            recommend the top 3 products. Return ONLY a JSON array:
            [{"productId": id, "reason": "why recommended"}]
            """;

        StringBuilder sb = new StringBuilder("User preferences: ").append(userPreferences).append("\nProducts:\n");
        for (Map<String, Object> p : products) {
            sb.append("- ID: ").append(p.get("id")).append(", Name: ").append(p.get("name"))
              .append(", Price: ").append(p.get("price"))
              .append(", Rating: ").append(p.get("rating")).append("\n");
        }

        return callAPI(systemPrompt, sb.toString());
    }

    private String callAPI(String systemPrompt, String userMessage) {
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "temperature", 0.5,
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
            log.error("Qwen API call failed", e);
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
            log.warn("Failed to parse Qwen JSON: {}", response);
            return Map.of("raw", response);
        }
    }
}
