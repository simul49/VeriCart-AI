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
import java.util.ArrayList;
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

    /**
     * Product-scoped chat — used as the fallback provider when Kimi is unavailable.
     */
    public String chat(String productName, String question, List<Map<String, Object>> reviews, List<Map<String, String>> history) {
        if (mockService.isEnabled()) {
            log.debug("[DeepSeek Mock] Simulating chat");
            return mockService.chat(productName, question, reviews);
        }

        String systemPrompt = """
            You are VeriCart AI, a friendly and knowledgeable shopping assistant — behave like
            a real conversational agent (e.g., DeepSeek Chat, Kimi, ChatGPT).

            Rules:
            • Answer ANY question the customer asks — product, shopping, general knowledge, or
              small talk. Never give a robotic canned response.
            • When they greet you (hi/hello/hey), reply with a warm, natural greeting and
              briefly offer what you can help with. Vary your opening each time.
            • When the question is about THIS product, ground your answer in the real review
              data below — cite themes (battery, quality, value, comfort, etc.) and be specific.
              If reviews are mixed, say so honestly.
            • Keep replies concise and natural (1 sentence for a simple greeting; 2–5 sentences
              for normal Q&A; a short list only when truly helpful).
            • Never invent specs, prices, or claims not supported by the data. If unsure, say so.
            • Do NOT start every reply with the same opening line. Vary phrasing.
            """;

        StringBuilder sb = new StringBuilder();
        sb.append("Product: ").append(productName).append("\n");
        sb.append("Customer Question: ").append(question).append("\n");
        sb.append("Available Reviews:\n");
        for (int i = 0; i < Math.min(reviews.size(), 15); i++) {
            Map<String, Object> r = reviews.get(i);
            sb.append("- Rating ").append(r.get("rating")).append("/5: ").append(r.get("content")).append("\n");
        }

        return callAPIWithHistory(systemPrompt, sb.toString(), history);
    }

    /**
     * General (no-product) chat — fallback provider when Kimi is unavailable.
     */
    public String generalChat(String question, List<Map<String, Object>> productContext, List<Map<String, String>> history) {
        if (mockService.isEnabled()) {
            log.debug("[DeepSeek Mock] Simulating general chat");
            return mockService.generalChat(question, productContext);
        }

        String systemPrompt = """
            You are VeriCart AI, a friendly shopping-assistant agent. You can chat about ANYTHING
            — general questions, advice, small talk — AND help the user find and compare products
            in this store.

            Rules:
            • Greet naturally and VARY your reply each time. Never repeat the same canned opener.
              Offer what you can help with (shopping, comparisons, recommendations, trust,
              reviews, returns, general chat).
            • For shopping questions, ground answers in the catalog list provided — name, price,
              rating, trust score of the most relevant items. Be honest about trade-offs.
            • For non-shopping questions, still help conversationally and briefly, then gently
              offer to relate it back to shopping if useful.
            • Keep replies concise (2–5 sentences). Warm, natural tone. No robotic templates.
            • Never invent product details not in the catalog. If you don't know, say so.
            """;

        StringBuilder sb = new StringBuilder("Customer Question: ").append(question).append("\n\n");
        sb.append("Available Products in Store:\n");
        for (Map<String, Object> p : productContext) {
            sb.append("- ").append(p.get("name"))
              .append(" | $").append(p.get("price"))
              .append(" | Rating: ").append(p.get("rating"))
              .append("/5 | Trust: ").append(p.get("trustScore") != null ? p.get("trustScore") : "N/A")
              .append(" | Brand: ").append(p.get("brand") != null ? p.get("brand") : "—")
              .append(" | Category: ").append(p.get("categoryId") != null ? p.get("categoryId") : "—")
              .append(" | ").append(p.get("description") != null ? p.get("description") : "")
              .append("\n");
        }

        return callAPIWithHistory(systemPrompt, sb.toString(), history);
    }

    private String doCall(List<Map<String, String>> messages) {
        if (keyIsPlaceholder) {
            throw new RuntimeException("DeepSeek API key is not configured. Set DEEPSEEK_API_KEY env var or enable ai.mock.enabled=true");
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", messages,
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

    private String callAPI(String systemPrompt, String userMessage) {
        return doCall(buildMessages(systemPrompt, userMessage, null));
    }

    private String callAPIWithHistory(String systemPrompt, String userMessage, List<Map<String, String>> history) {
        return doCall(buildMessages(systemPrompt, userMessage, history));
    }

    private List<Map<String, String>> buildMessages(String systemPrompt, String userMessage, List<Map<String, String>> history) {
        List<Map<String, String>> msgs = new ArrayList<>();
        msgs.add(Map.of("role", "system", "content", systemPrompt));
        if (history != null) {
            for (Map<String, String> h : history) {
                String role = h.get("role");
                String content = h.get("content");
                if (role != null && content != null && !content.isBlank()) {
                    msgs.add(Map.of("role", role, "content", content));
                }
            }
        }
        msgs.add(Map.of("role", "user", "content", userMessage));
        return msgs;
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
