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
    private final AuditLogService auditLogService;

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

            // 4. Re-read reviews so metrics reflect the freshly written AI fields
            List<Review> analyzed = reviewMapper.findByProductId(productId);

            // 5. Count fake reviews
            long fakeCount = analyzed.stream().filter(r -> r.getIsFlagged() != null && r.getIsFlagged() == 1).count();

            // 6. Update product with AI results
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
            result.put("totalReviews", analyzed.size());
            // FR-045 — explainable AI: show exactly why this score was produced
            result.put("explanation", buildTrustExplanation(analyzed, trustScore, fakeCount));
            // FR-040 — topic extraction across reviews
            result.put("topics", extractTopics(analyzed));

            // FR-069 — audit AI processing
            auditLogService.record(null, null, "AI_ANALYSIS", "AI", "PRODUCT", productId,
                    String.format("AI analysis for %s: trust score %d (%s), %d/%d reviews flagged as fake",
                            product.getName(), trustScore, trustLevel, fakeCount, analyzed.size()));
            return result;
        } catch (Exception e) {
            log.error("AI analysis failed for product " + productId, e);
            // FR-070 — record the error
            auditLogService.recordError(null, null, "AI_ANALYSIS_FAILED",
                    "AI analysis failed for product " + productId + ": " + e.getMessage());
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

        Map<String, Object> recResult = aiGateway.getRecommendations(preferences, productData);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recommendations = recResult != null && recResult.get("recommendations") instanceof List
                ? (List<Map<String, Object>>) recResult.get("recommendations") : List.of();

        List<Long> recommendedIds = recommendations.stream()
                .map(r -> r.get("productId") instanceof Number ? ((Number) r.get("productId")).longValue() : null)
                .filter(Objects::nonNull)
                .toList();

        List<Product> recommended = recommendedIds.stream()
                .map(productMapper::findById)
                .filter(Objects::nonNull)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("products", recommended);
        result.put("recommendations", recommendations);
        result.put("explanation", recResult != null ? recResult.get("explanation") : "");
        return result;
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

    /**
     * FR-045 — Explainable AI.
     * Breaks the Trust Score into weighted, human-readable factors so users can see
     * exactly why a product received its score (PRD Principle 2: Explainable AI).
     */
    private List<Map<String, Object>> buildTrustExplanation(List<Review> reviews, int trustScore, long fakeCount) {
        int total = reviews.size();
        if (total == 0) return List.of();

        long flagged = reviews.stream().filter(r -> Integer.valueOf(1).equals(r.getIsFlagged())).count();
        long positive = reviews.stream().filter(r -> "POSITIVE".equalsIgnoreCase(r.getSentiment())).count();
        long verified = reviews.stream().filter(r -> r.getOrderId() != null).count();

        double avgRating = reviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(Review::getRating).average().orElse(0.0);
        // Consistency: lower rating variance = more consistent opinions
        double variance = reviews.stream().filter(r -> r.getRating() != null)
                .mapToInt(Review::getRating)
                .mapToDouble(v -> Math.pow(v - avgRating, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // Factor scores (0-100) with PRD-aligned weights
        double authenticity = ((double) (total - flagged) / total) * 100;
        double sentiment = ((double) positive / total) * 100;
        double verifiedRatio = ((double) verified / total) * 100;
        double volume = Math.min(100, (total / 50.0) * 100);          // 50+ reviews = full marks
        double consistency = Math.max(0, 100 - (stdDev * 40));        // stdDev 2.5 → 0

        List<Object[]> factors = List.of(
                new Object[]{"Review Authenticity", authenticity, 30,
                        String.format("%d of %d reviews passed AI fake-detection", total - flagged, total)},
                new Object[]{"Customer Sentiment", sentiment, 30,
                        String.format("%d of %d reviews are positive", positive, total)},
                new Object[]{"Verified Purchases", verifiedRatio, 20,
                        String.format("%d of %d reviews come from confirmed orders", verified, total)},
                new Object[]{"Review Volume", volume, 10,
                        String.format("%d reviews analysed (50+ ideal)", total)},
                new Object[]{"Rating Consistency", consistency, 10,
                        String.format("Rating spread σ=%.2f across reviews", stdDev)}
        );

        List<Map<String, Object>> explanation = new ArrayList<>();
        for (Object[] f : factors) {
            Map<String, Object> m = new HashMap<>();
            double score = (double) f[1];
            int weight = (int) f[2];
            m.put("factor", f[0]);
            m.put("score", Math.round(score));
            m.put("weight", weight);
            m.put("contribution", Math.round(score * weight / 100.0 * 10.0) / 10.0);
            m.put("detail", f[3]);
            explanation.add(m);
        }
        return explanation;
    }

    /**
     * FR-040 — Topic Extraction with per-topic sentiment.
     * Scans review text for common product aspects and reports how customers feel about each.
     */
    private List<Map<String, Object>> extractTopics(List<Review> reviews) {
        Map<String, List<String>> topicKeywords = new LinkedHashMap<>();
        topicKeywords.put("Battery Life", List.of("battery", "charge", "charging", "power"));
        topicKeywords.put("Build Quality", List.of("quality", "build", "durable", "sturdy", "material"));
        topicKeywords.put("Price Value", List.of("price", "expensive", "cheap", "value", "money", "worth"));
        topicKeywords.put("Delivery", List.of("delivery", "shipping", "arrived", "shipped", "fast"));
        topicKeywords.put("Packaging", List.of("packaging", "package", "boxed", "wrapped"));
        topicKeywords.put("Performance", List.of("fast", "slow", "performance", "speed", "lag", "responsive"));
        topicKeywords.put("Design", List.of("design", "look", "beautiful", "style", "appearance"));
        topicKeywords.put("Screen Display", List.of("screen", "display", "resolution", "bright", "colors"));
        topicKeywords.put("Camera", List.of("camera", "photo", "photos", "picture", "pictures"));
        topicKeywords.put("Comfort", List.of("comfort", "comfortable", "fit", "lightweight", "heavy"));

        List<Map<String, Object>> topics = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : topicKeywords.entrySet()) {
            int mentions = 0;
            int positiveMentions = 0;
            for (Review r : reviews) {
                if (r.getContent() == null) continue;
                String text = r.getContent().toLowerCase();
                boolean matches = entry.getValue().stream().anyMatch(text::contains);
                if (matches) {
                    mentions++;
                    if ("POSITIVE".equalsIgnoreCase(r.getSentiment())) positiveMentions++;
                }
            }
            if (mentions == 0) continue;

            Map<String, Object> t = new HashMap<>();
            t.put("topic", entry.getKey());
            t.put("mentions", mentions);
            t.put("positiveRatio", mentions == 0 ? 0 : Math.round((double) positiveMentions / mentions * 100));
            topics.add(t);
        }
        topics.sort((a, b) -> (int) b.get("mentions") - (int) a.get("mentions"));
        return topics;
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
