package com.vericart.service;

import com.vericart.ai.AiGateway;
import com.vericart.dto.AiChatRequest;
import com.vericart.dto.AiChatResponse;
import com.vericart.entity.Product;
import com.vericart.entity.Review;
import com.vericart.mapper.ProductMapper;
import com.vericart.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiGateway aiGateway;
    private final ReviewMapper reviewMapper;
    private final ProductMapper productMapper;
    private final ReviewService reviewService;

    /**
     * Full AI analysis pipeline for a product
     */
    @Transactional
    public Map<String, Object> analyzeProduct(Long productId) {
        Product product = productMapper.findById(productId);
        if (product == null) throw new com.vericart.exception.BusinessException(404, "Product not found");

        List<Review> reviews = reviewMapper.findByProductId(productId);
        if (reviews.isEmpty()) {
            return Map.of("message", "No reviews to analyze yet", "productId", productId);
        }

        // Convert reviews for AI processing
        List<Map<String, Object>> reviewData = reviews.stream()
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", r.getId());
                    m.put("rating", r.getRating());
                    m.put("content", r.getContent() != null ? r.getContent() : "");
                    return m;
                })
                .toList();

        try {
            // 1. Analyze individual reviews (sentiment + fake detection)
            for (Review review : reviews) {
                if (review.getSentiment() == null && review.getContent() != null && !review.getContent().isBlank()) {
                    try {
                        // Sentiment via Qwen
                        Map<String, Object> sentiment = aiGateway.analyzeSentiment(review.getContent());
                        String sent = sentiment.get("sentiment") != null ? sentiment.get("sentiment").toString() : null;
                        String emotion = sentiment.get("emotion") != null ? sentiment.get("emotion").toString() : null;

                        // Fake detection via DeepSeek
                        Map<String, Object> fakeResult = aiGateway.detectFakeReview(review.getContent(), product.getName());
                        Object probObj = fakeResult.get("probability");
                        BigDecimal fakeProb = probObj instanceof Number
                                ? BigDecimal.valueOf(((Number) probObj).doubleValue()) : null;
                        String fakeReason = fakeResult.get("reasons") != null ? fakeResult.get("reasons").toString() : null;
                        boolean isSuspicious = Boolean.TRUE.equals(fakeResult.get("isSuspicious"));

                        reviewService.updateAiFields(review.getId(), sent, emotion, fakeProb, fakeReason, isSuspicious ? 1 : 0);
                    } catch (Exception e) {
                        log.warn("AI analysis failed for review {}: {}", review.getId(), e.getMessage());
                    }
                }
            }

            // 2. Generate trust score (multi-model orchestration)
            Map<String, Object> trustResult = aiGateway.generateTrustScore(product.getName(), reviewData);
            int trustScore = trustResult.get("trustScore") instanceof Number
                    ? ((Number) trustResult.get("trustScore")).intValue() : 50;
            String trustLevel = trustResult.get("trustLevel") != null
                    ? trustResult.get("trustLevel").toString() : "Medium";

            // 3. Generate review summary via Hunyuan
            Map<String, Object> summary = aiGateway.generateReviewSummary(reviewData, product.getName());

            // 4. Count fake reviews
            long fakeCount = reviews.stream().filter(r -> r.getIsFlagged() != null && r.getIsFlagged() == 1).count();

            // 5. Update product with AI results
            product.setTrustScore(trustScore);
            product.setTrustLevel(trustLevel);
            product.setFakeReviewCount((int) fakeCount);

            String aiSummary = summary != null ? buildSummaryText(summary) : null;
            product.setAiSummary(aiSummary);
            product.setAiSummaryTime(java.time.LocalDateTime.now());

            productMapper.updateAiFields(product);

            Map<String, Object> result = new HashMap<>();
            result.put("trustScore", trustScore);
            result.put("trustLevel", trustLevel);
            result.put("fakeReviewCount", fakeCount);
            result.put("summary", summary);
            result.put("totalReviews", reviews.size());
            return result;
        } catch (Exception e) {
            log.error("AI analysis failed for product " + productId, e);
            throw new com.vericart.exception.BusinessException(500, "AI analysis failed: " + e.getMessage());
        }
    }

    /**
     * AI Shopping Assistant chat (product-specific)
     */
    public AiChatResponse chat(Long userId, AiChatRequest request) {
        Product product = productMapper.findById(request.getProductId());
        if (product == null) throw new com.vericart.exception.BusinessException(404, "Product not found");

        List<Review> reviews = reviewMapper.findByProductId(request.getProductId());
        List<Map<String, Object>> reviewData = reviews.stream()
                .map(r -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("rating", r.getRating());
                    m.put("content", r.getContent() != null ? r.getContent() : "");
                    return m;
                }).toList();

        return aiGateway.chat(request.getProductId(), product.getName(), request.getQuestion(), reviewData);
    }

    /**
     * AI Shopping Assistant general chat (no product context)
     */
    public AiChatResponse generalChat(Long userId, String question) {
        // Get all products with reviews for context
        List<Product> allProducts = productMapper.findAll(0, 20, null, null, null);
        List<Map<String, Object>> productContext = allProducts.stream()
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    m.put("price", p.getPrice());
                    m.put("rating", p.getRating());
                    m.put("trustScore", p.getTrustScore());
                    return m;
                }).toList();

        return aiGateway.generalChat(question, productContext);
    }

    /**
     * AI Product Recommendations
     */
    public Map<String, Object> recommend(Long userId, String preferences) {
        List<Product> allProducts = productMapper.findAll(0, 50, null, null, null);

        List<Map<String, Object>> productData = allProducts.stream()
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    m.put("price", p.getPrice());
                    m.put("description", p.getDescription() != null ? p.getDescription() : "");
                    m.put("rating", p.getRating());
                    m.put("trustScore", p.getTrustScore());
                    m.put("category", p.getCategoryId());
                    m.put("reviewCount", p.getReviewCount());
                    return m;
                }).toList();

        String recommendationJson = aiGateway.getRecommendations(preferences, productData);

        // Parse the recommendation response
        List<Long> recommendedIds = parseRecommendedIds(recommendationJson);
        List<Product> recommended = recommendedIds.stream()
                .map(productMapper::findById)
                .filter(Objects::nonNull)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("products", recommended);
        result.put("explanation", recommendationJson);
        return result;
    }

    private List<Long> parseRecommendedIds(String json) {
        // Try to extract product IDs from AI response
        List<Long> ids = new ArrayList<>();
        try {
            // Simple extraction: look for "id" fields in JSON-like response
            String[] lines = json.split("[\\n,]");
            for (String line : lines) {
                if (line.contains("\"id\"") || line.contains("'id'")) {
                    String numStr = line.replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        try {
                            ids.add(Long.parseLong(numStr));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse recommendation IDs: {}", e.getMessage());
        }
        return ids;
    }

    @Async
    @Transactional
    public void batchAnalyzePending() {
        List<Review> pendingReviews = reviewMapper.findReviewsNeedingAnalysis(20);
        List<Product> pendingProducts = productMapper.findProductsNeedingAiUpdate(10);

        log.info("[AI Batch] Processing {} reviews and {} products", pendingReviews.size(), pendingProducts.size());

        for (Product product : pendingProducts) {
            try {
                analyzeProduct(product.getId());
            } catch (Exception e) {
                log.error("[AI Batch] Failed to analyze product {}: {}", product.getId(), e.getMessage());
            }
        }
    }

    private String buildSummaryText(Map<String, Object> summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("### AI Review Summary\n\n");

        if (summary.containsKey("advantages")) {
            sb.append("**Advantages:**\n");
            Object adv = summary.get("advantages");
            if (adv instanceof List) {
                for (Object a : (List<?>) adv) sb.append("- ").append(a).append("\n");
            }
        }

        if (summary.containsKey("disadvantages")) {
            sb.append("\n**Common Issues:**\n");
            Object dis = summary.get("disadvantages");
            if (dis instanceof List) {
                for (Object d : (List<?>) dis) sb.append("- ").append(d).append("\n");
            }
        }

        if (summary.containsKey("overallOpinion")) {
            sb.append("\n**Overall:** ").append(summary.get("overallOpinion")).append("\n");
        }

        if (summary.containsKey("recommendation")) {
            sb.append("\n**AI Recommendation:** ").append(summary.get("recommendation"));
            if (summary.containsKey("recommendationReason")) {
                sb.append(" (").append(summary.get("recommendationReason")).append(")");
            }
        }

        return sb.toString();
    }
}
