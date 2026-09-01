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
    private final AiMockService mockService;
    private final boolean keyIsPlaceholder;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public QwenClient(@Value("${ai.qwen.api-key}") String apiKey,
                      @Value("${ai.qwen.base-url}") String baseUrl,
                      @Value("${ai.qwen.model}") String model,
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
            log.error("Qwen API key is a placeholder and mock mode is OFF. AI calls will fail.");
        }
    }

    public Map<String, Object> analyzeSentiment(String reviewContent) {
        if (mockService.isEnabled()) {
            log.debug("[Qwen Mock] Simulating sentiment analysis");
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

        String response = callAPI(systemPrompt, reviewContent);
        return parseJson(response);
    }

    public Map<String, Object> analyzeSentimentBatch(List<Map<String, Object>> reviews) {
        // Always use the heuristic calculation — no real API needed for batch stats
        return mockService.analyzeSentimentBatch(reviews);
    }

    public String chat(String productName, String question, List<Map<String, Object>> reviews) {
        if (mockService.isEnabled()) {
            log.debug("[Qwen Mock] Simulating chat");
            return mockService.chat(productName, question, reviews);
        }

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

    /**
     * AI auto-reply for customer product inquiries — answers as the store's assistant
     * while the seller is unavailable. Uses the product card + conversation history
     * as context so EVERY question (including follow-ups) gets a proper answer.
     */
    public String answerProductQuestion(Map<String, Object> productInfo, String question,
                                        List<Map<String, Object>> history) {
        if (mockService.isEnabled()) {
            log.debug("[Qwen Mock] Simulating inquiry auto-reply");
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

        return callAPI(systemPrompt, ctx.toString());
    }

    public String generalChat(String question, List<Map<String, Object>> productContext) {
        if (mockService.isEnabled()) {
            log.debug("[Qwen Mock] Simulating general chat");
            return mockService.generalChat(question, productContext);
        }

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

    public Map<String, Object> recommend(String userPreferences, List<Map<String, Object>> products) {
        if (mockService.isEnabled()) {
            log.debug("[Qwen Mock] Simulating recommendations");
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
              .append(", TrustScore: ").append(p.get("trustScore")).append("\n");
        }

        String response = callAPI(systemPrompt, sb.toString());
        return parseJson(response);
    }

    private String callAPI(String systemPrompt, String userMessage) {
        if (keyIsPlaceholder) {
            throw new RuntimeException("Qwen API key is not configured. Set QWEN_API_KEY env var or enable ai.mock.enabled=true");
        }

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
            int statusCode = response.statusCode();

            if (statusCode != 200) {
                String errorBody = response.body();
                log.error("Qwen API returned HTTP {}: {}", statusCode, errorBody);
                throw new RuntimeException(String.format("Qwen API error (HTTP %d): %s", statusCode,
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
            throw new RuntimeException("Qwen API returned no choices in response: " + response.body());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Qwen API call failed: " + e.getMessage(), e);
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
