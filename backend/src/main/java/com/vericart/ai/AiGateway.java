package com.vericart.ai;

import com.vericart.dto.AiChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiGateway {

    private final DeepSeekClient deepSeekClient;
    private final QwenClient qwenClient;
    private final HunyuanClient hunyuanClient;

    /**
     * Detect fake review — uses DeepSeek (best at reasoning)
     */
    public Map<String, Object> detectFakeReview(String reviewContent, String productName) {
        log.info("[AI Gateway] Routing fake-review detection → DeepSeek");
        return deepSeekClient.detectFakeReview(reviewContent, productName);
    }

    /**
     * Analyze sentiment — uses Qwen
     */
    public Map<String, Object> analyzeSentiment(String reviewContent) {
        log.info("[AI Gateway] Routing sentiment analysis → Qwen");
        return qwenClient.analyzeSentiment(reviewContent);
    }

    /**
     * Generate product review summary — uses Hunyuan (Chinese + summarization)
     */
    public Map<String, Object> generateReviewSummary(List<Map<String, Object>> reviews, String productName) {
        log.info("[AI Gateway] Routing review summary → Hunyuan");
        return hunyuanClient.generateReviewSummary(reviews, productName);
    }

    /**
     * Generate trust score — orchestrates DeepSeek + Qwen + Hunyuan
     */
    public Map<String, Object> generateTrustScore(String productName, List<Map<String, Object>> reviews) {
        log.info("[AI Gateway] Generating Trust Score (multi-model orchestration)");

        // 1. DeepSeek: fake review check + trust reasoning
        Map<String, Object> deepSeekResult = null;
        try {
            deepSeekResult = deepSeekClient.analyzeTrust(productName, reviews);
        } catch (Exception e) {
            log.warn("DeepSeek trust analysis failed, using fallback", e);
        }

        // 2. Qwen: sentiment overview
        Map<String, Object> qwenResult = null;
        try {
            qwenResult = qwenClient.analyzeSentimentBatch(reviews);
        } catch (Exception e) {
            log.warn("Qwen sentiment analysis failed, using fallback", e);
        }

        // 3. Compute aggregated trust score
        int score = computeTrustScore(deepSeekResult, qwenResult, reviews.size());
        String level = score >= 85 ? "Excellent" : score >= 70 ? "High" : score >= 50 ? "Medium" : "Low";

        return Map.of(
                "trustScore", score,
                "trustLevel", level,
                "totalReviews", reviews.size(),
                "deepSeekAnalysis", deepSeekResult != null ? deepSeekResult : Map.of(),
                "qwenAnalysis", qwenResult != null ? qwenResult : Map.of()
        );
    }

    /**
     * AI Shopping Assistant chat — uses Qwen
     */
    public AiChatResponse chat(Long productId, String productName, String question,
                               List<Map<String, Object>> reviews) {
        log.info("[AI Gateway] Routing chat → Qwen for product={}", productId);
        String answer = qwenClient.chat(productName, question, reviews);
        return new AiChatResponse(answer, "qwen-plus", productId);
    }

    /**
     * General chat (no specific product context)
     */
    public AiChatResponse generalChat(String question, List<Map<String, Object>> productContext) {
        log.info("[AI Gateway] Routing general chat → Qwen");
        String answer = qwenClient.generalChat(question, productContext);
        return new AiChatResponse(answer, "qwen-plus", null);
    }

    /**
     * Get product recommendations
     */
    public String getRecommendations(String userPreferences, List<Map<String, Object>> products) {
        log.info("[AI Gateway] Routing recommendations → Qwen");
        return qwenClient.recommend(userPreferences, products);
    }

    private int computeTrustScore(Map<String, Object> deepSeek, Map<String, Object> qwen, int reviewCount) {
        int baseScore = 70;

        if (deepSeek != null) {
            Object trustObj = deepSeek.get("trustScore");
            if (trustObj instanceof Number) {
                baseScore = ((Number) trustObj).intValue();
            }
        }

        if (qwen != null) {
            Object posObj = qwen.get("positiveRatio");
            if (posObj instanceof Number) {
                double pos = ((Number) posObj).doubleValue();
                baseScore = (int) (baseScore * 0.6 + pos * 40);
            }
        }

        if (reviewCount < 3) baseScore = Math.min(baseScore, 60);
        return Math.max(0, Math.min(100, baseScore));
    }
}
