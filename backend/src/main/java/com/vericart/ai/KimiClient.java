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
public class KimiClient {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final AiMockService mockService;
    private final boolean keyIsPlaceholder;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public KimiClient(@Value("${ai.kimi.api-key}") String apiKey,
                      @Value("${ai.kimi.base-url}") String baseUrl,
                      @Value("${ai.kimi.model}") String model,
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
            log.error("Kimi (Moonshot) API key is a placeholder and mock mode is OFF. AI calls will fall back or fail.");
        }
    }

    /** True when a real (non-placeholder) API key is configured. */
    public boolean isConfigured() {
        return !keyIsPlaceholder;
    }

    /**
     * Product-scoped chat — the AI Shopping Assistant for a single product.
     * Prefers the real Kimi (Moonshot) API; when mock mode is on it returns the
     * simulated answer so local/demo runs need no key.
     */
    public String chat(String productName, String question, List<Map<String, Object>> reviews, List<Map<String, String>> history) {
        if (mockService.isEnabled()) {
            log.debug("[Kimi Mock] Simulating chat");
            return mockService.chat(productName, question, reviews);
        }

        String systemPrompt = """
            You are VeriCart AI Shopping Assistant. Help customers make informed purchasing decisions.
            Be honest, transparent, and base your answers on the actual review data provided.
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

        return callAPIWithHistory(systemPrompt, sb.toString(), history);
    }

    /**
     * General (no-product) chat — the global AI Shopping Assistant.
     * Answers ANY customer question in a friendly, helpful tone, using the store's
     * catalog as context when relevant.
     */
    public String generalChat(String question, List<Map<String, Object>> productContext, List<Map<String, String>> history) {
        if (mockService.isEnabled()) {
            log.debug("[Kimi Mock] Simulating general chat");
            return mockService.generalChat(question, productContext);
        }

        String systemPrompt = """
            You are VeriCart AI Shopping Assistant. Help customers with general shopping questions,
            product comparisons, purchasing advice, and anything else they ask. You have access to the
            store's product catalog for context. Be helpful, honest, and transparent. If you recommend a
            product, explain why. Answer ANY question the customer asks — not just shopping ones — in a
            friendly tone. Keep answers concise (3-5 sentences).
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

    /**
     * Single-review sentiment analysis — replaces the former Qwen role.
     * Returns a JSON map {sentiment, emotion, score, keyPoints}.
     */
    public Map<String, Object> analyzeSentiment(String reviewContent) {
        if (mockService.isEnabled()) {
            log.debug("[Kimi Mock] Simulating sentiment analysis");
            return mockService.analyzeSentiment(reviewContent);
        }

        String systemPrompt = """
            Analyze customer review sentiment. Return ONLY a JSON:
            {
              "sentiment": "POSITIVE/NEUTRAL/NEGATIVE",
              "emotion": "happy/satisfied/neutral/disappointed/angry",
              "score": <1-10>,
              "keyPoints": ["point1", "point2"]
            }
            """;

        String response = doCall(buildMessages(systemPrompt, reviewContent, null));
        return parseJson(response);
    }

    /**
     * Batch sentiment overview across the whole review set — uses the heuristic
     * calculation (no real API needed), consistent with the prior Qwen behaviour.
     */
    public Map<String, Object> analyzeSentimentBatch(List<Map<String, Object>> reviews) {
        // Always use the heuristic calculation — no real API needed for batch stats
        return mockService.analyzeSentimentBatch(reviews);
    }

    /**
     * Store-assistant auto-reply for product inquiries while the seller is away.
     * Uses the product card + conversation history as context (replaces Qwen role).
     */
    public String answerProductQuestion(Map<String, Object> productInfo, String question,
                                        List<Map<String, Object>> history) {
        if (mockService.isEnabled()) {
            log.debug("[Kimi Mock] Simulating inquiry auto-reply");
            return mockService.answerProductQuestion(productInfo, question, history);
        }

        String systemPrompt = """
            You are the assistant of a store on the VeriCart AI marketplace.
            A customer is chatting with the store, and the owner is currently unavailable.
            Reply to the customer directly, in the same language as their latest message.
            Use the conversation history to understand follow-up questions ("it", "the
            charger", "when exactly", etc. refer to earlier messages).
            Answer ONLY based on the product facts provided (name, brand, price, stock,
            description, rating, trust score, review summary). If the facts don't answer
            the question, say the owner will confirm details as soon as they return.
            Be friendly, concise (3-5 sentences), honest, and never invent stock/prices.
            """;

        StringBuilder ctx = new StringBuilder();
        if (productInfo != null && !productInfo.isEmpty()) {
            ctx.append("### Product Card\n");
            ctx.append("Name: ").append(productInfo.getOrDefault("name", "?")).append("\n");
            ctx.append("Brand: ").append(productInfo.getOrDefault("brand", "N/A")).append("\n");
            ctx.append("Price: $").append(productInfo.getOrDefault("price", "N/A")).append("\n");
            ctx.append("Stock: ").append(productInfo.getOrDefault("stock", "N/A")).append("\n");
            ctx.append("Rating: ").append(productInfo.getOrDefault("rating", "N/A")).append("/5\n");
            ctx.append("Trust score: ").append(productInfo.getOrDefault("trustScore", "N/A")).append("/100\n");
            ctx.append("Description: ").append(productInfo.getOrDefault("description", "")).append("\n");
            if (productInfo.get("aiSummary") != null) {
                ctx.append("AI review summary: ").append(productInfo.get("aiSummary")).append("\n");
            }
        }
        if (history != null && !history.isEmpty()) {
            ctx.append("\n### Conversation history (oldest → newest)\n");
            for (Map<String, Object> m : history) {
                ctx.append("[").append(m.getOrDefault("sender", "?")).append("] ")
                  .append(m.getOrDefault("content", "")).append("\n");
            }
        }
        ctx.append("\n### Customer Message\n").append(question);

        List<Map<String, String>> histStr = new ArrayList<>();
        if (history != null) {
            for (Map<String, Object> m : history) {
                String role = String.valueOf(m.getOrDefault("role", ""));
                String content = String.valueOf(m.getOrDefault("content", ""));
                if (!content.isBlank()) histStr.add(Map.of("role", role, "content", content));
            }
        }
        return callAPIWithHistory(systemPrompt, ctx.toString(), histStr);
    }

    /**
     * Product recommendations based on user preferences — replaces the Qwen role.
     */
    public Map<String, Object> recommend(String userPreferences, List<Map<String, Object>> products) {
        if (mockService.isEnabled()) {
            log.debug("[Kimi Mock] Simulating recommendations");
            return mockService.recommend(userPreferences, products);
        }

        String systemPrompt = """
            You are a product recommendation expert. Based on user preferences and available products,
            recommend the top 3 products. Return ONLY a JSON object with this structure:
            {
              "recommendations": [
                {"productId": 1, "name": "Product Name", "price": 99.99, "rating": 4.5, "trustScore": 85, "reason": "why recommended", "highlights": ["highlight1"]}
              ],
              "explanation": "Markdown explanation of why these products were chosen"
            }
            """;

        StringBuilder sb = new StringBuilder("User preferences: ").append(userPreferences).append("\nProducts:\n");
        for (Map<String, Object> p : products) {
            sb.append("- ID: ").append(p.get("id")).append(", Name: ").append(p.get("name"))
              .append(", Price: ").append(p.get("price"))
              .append(", Rating: ").append(p.get("rating"))
              .append(", TrustScore: ").append(p.get("trustScore"))
              .append(", Brand: ").append(p.get("brand") != null ? p.get("brand") : "—")
              .append(", Description: ").append(p.get("description") != null ? p.get("description") : "")
              .append("\n");
        }

        String response = doCall(buildMessages(systemPrompt, sb.toString(), null));
        return parseJson(response);
    }

    private Map<String, Object> parseJson(String response) {
        try {
            if (response == null) return Map.of();
            String json = response;
            // Strip <think>...</think> reasoning blocks (kimi-k3 may emit them before the JSON)
            json = json.replaceAll("(?s)<think>.*?</think>", "");
            // Extract fenced JSON if present
            if (json.contains("```json")) {
                int s = json.indexOf("```json") + 7;
                int e = json.indexOf("```", s);
                json = e > s ? json.substring(s, e) : json.substring(s);
            } else if (json.contains("```")) {
                int s = json.indexOf("```") + 3;
                int e = json.indexOf("```", s);
                json = e > s ? json.substring(s, e) : json.substring(s);
            } else {
                // No fences: grab the first balanced JSON object
                int first = json.indexOf('{');
                int last = json.lastIndexOf('}');
                if (first >= 0 && last > first) {
                    json = json.substring(first, last + 1);
                }
            }
            return objectMapper.readValue(json.trim(), new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse Kimi JSON: {}", response);
            return Map.of("raw", response);
        }
    }

    private String doCall(List<Map<String, String>> messages) {
        if (keyIsPlaceholder) {
            throw new RuntimeException("Kimi (Moonshot) API key is not configured. Set KIMI_API_KEY env var or enable ai.mock.enabled=true");
        }

        try {
            // kimi-k3 on TokenHub requires temperature=1.0 (only 1 is allowed for this model).
            // Other Kimi/Moonshot models default to 0.5.
            double temperature = "kimi-k3".equals(model) ? 1.0 : 0.5;
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", messages,
                    "temperature", temperature,
                    "max_tokens", 4000
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(90))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode != 200) {
                String errorBody = response.body();
                log.error("Kimi API returned HTTP {}: {}", statusCode, errorBody);
                throw new RuntimeException(String.format("Kimi API error (HTTP %d): %s", statusCode,
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
            throw new RuntimeException("Kimi API returned no choices in response: " + response.body());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Kimi API call failed: " + e.getMessage(), e);
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
}
