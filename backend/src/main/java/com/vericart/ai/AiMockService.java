package com.vericart.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Simulated AI service for local development/demo without real API keys.
 * Generates realistic-looking responses based on review data heuristics.
 */
@Slf4j
@Component
public class AiMockService {

    private final boolean enabled;

    public AiMockService(@Value("${ai.mock.enabled:true}") boolean enabled) {
        this.enabled = enabled;
        log.info("[AI Mock] Mock mode: {}", enabled ? "ENABLED" : "DISABLED");
    }

    public boolean isEnabled() { return enabled; }

    // --- Fake Review Detection (DeepSeek simulator) ---

    /**
     * Heuristic fake-review detector that simulates DeepSeek when running without
     * real API keys. Combines several spam signals into a 0–100 probability so the
     * demo shows meaningful, explainable results.
     */
    public Map<String, Object> detectFakeReview(String reviewContent, String productName) {
        if (!enabled) return null;

        String text = reviewContent == null ? "" : reviewContent;
        String lower = text.toLowerCase();
        int len = text.length();
        List<String> reasons = new ArrayList<>();
        int score = 10; // base prior — most reviews are genuine

        // 1. Excessive punctuation — "Great product!!!"
        int exclamations = (int) text.chars().filter(c -> c == '!').count();
        if (exclamations >= 3 || text.contains("!!!")) {
            score += 32;
            reasons.add("Excessive punctuation (" + exclamations + " exclamation marks)");
        }

        // 2. Promotional / spam phrasing
        String[] promo = {"buy now", "click here", "guaranteed", "limited offer", "cheapest price"};
        for (String p : promo) {
            if (lower.contains(p)) {
                score += 32;
                reasons.add("Promotional phrasing detected (\"" + p + "\")");
                break;
            }
        }

        // 3. Generic praise vocabulary
        String[] generic = {"amazing", "excellent", "perfect", "great", "good", "best", "quality",
                            "recommend", "nice", "wonderful", "fantastic", "superb", "awesome"};
        long genericHits = Arrays.stream(generic).filter(lower::contains).count();

        // Does the review contain concrete detail? Genuine reviews usually do.
        boolean hasSpecifics = text.matches(".*\\d.*") || lower.contains("battery") || lower.contains("screen")
                || lower.contains("because") || lower.contains("however") || lower.contains("only")
                || lower.contains("although") || lower.contains("compared") || lower.contains("than")
                || lower.contains("issue") || lower.contains("complaint") || lower.contains("month");

        // 4. Keyword stuffing — many praise terms packed into a short review
        if (genericHits >= 4 && len < 170) {
            score += 28;
            reasons.add("Keyword stuffing — " + genericHits + " generic praise terms in a short review");
        }

        // 5. Praise vocabulary with zero concrete detail — classic fake signature
        if (genericHits >= 3 && !hasSpecifics) {
            score += 32;
            reasons.add("Generic praise with no verifiable product specifics");
        }

        // 6. Unusually short
        if (len < 60) {
            score += 22;
            reasons.add("Unusually short review with no specific detail");
        }

        // 7. Superlative overload — "best … ever", repeated praise words
        if (lower.matches(".*best.*ever.*") || countOccurrences(lower, "amazing") >= 2) {
            score += 15;
            reasons.add("Superlative overload");
        }

        // 8. Word repetition — "good good good"
        if (hasRepeatedWord(lower)) {
            score += 18;
            reasons.add("Repetitive wording");
        }

        // 9. Mostly capitalised (shouting)
        long letters = text.chars().filter(Character::isLetter).count();
        long upper = text.chars().filter(Character::isUpperCase).count();
        if (letters > 10 && (double) upper / letters > 0.5) {
            score += 15;
            reasons.add("Excessive capitalisation");
        }

        // 10. Credit where due — detailed reviews are more credible
        if (hasSpecifics && len > 80) {
            score -= 15;
        }

        int probability = Math.max(2, Math.min(97, score + randomInt(-3, 3)));
        boolean suspicious = probability >= 60;

        if (reasons.isEmpty()) {
            reasons.add("Natural language patterns");
            reasons.add(hasSpecifics ? "Specific product details present" : "Balanced tone");
        }

        Map<String, Object> out = new HashMap<>();
        out.put("probability", probability);
        out.put("isSuspicious", suspicious);
        out.put("reasons", String.join("; ", reasons));
        out.put("confidence", randomInt(72, 96));
        return out;
    }

    private int countOccurrences(String haystack, String needle) {
        int count = 0, idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    /** True when any meaningful word is repeated three or more times. */
    private boolean hasRepeatedWord(String lower) {
        String[] words = lower.replaceAll("[^a-z0-9 ]", " ").split("\\s+");
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            if (w.length() < 4) continue;
            if (freq.merge(w, 1, Integer::sum) >= 3) return true;
        }
        return false;
    }

    /**
     * Simulated DeepSeek trust analysis. Derives a score from average rating,
     * the proportion of reviews that look spammy, and how much volume there is —
     * so products with planted fake reviews score noticeably lower.
     */
    public Map<String, Object> analyzeTrust(String productName, List<Map<String, Object>> reviews) {
        if (!enabled) return null;

        int total = reviews.size();
        double avgRating = reviews.stream()
                .mapToDouble(r -> ((Number) r.getOrDefault("rating", 3)).doubleValue())
                .average().orElse(3.0);

        // How many of these reviews look spammy?
        long spamCount = reviews.stream().filter(r -> {
            Object content = r.get("content");
            if (content == null) return false;
            Map<String, Object> d = detectFakeReview(content.toString(), productName);
            return d != null && Boolean.TRUE.equals(d.get("isSuspicious"));
        }).count();

        double spamRatio = total == 0 ? 0 : (double) spamCount / total;

        // Rating spread — wildly inconsistent ratings are less trustworthy
        double variance = reviews.stream()
                .mapToDouble(r -> ((Number) r.getOrDefault("rating", 3)).doubleValue())
                .map(v -> Math.pow(v - avgRating, 2))
                .average().orElse(0.0);

        double score = avgRating * 20;          // rating drives most of the score
        score -= spamRatio * 45;                // heavy penalty for suspicious reviews
        score -= Math.min(10, variance * 3);    // inconsistency penalty
        score += Math.min(8, total / 6.0);      // volume bonus (capped)
        score += randomInt(-3, 3);

        int trustScore = (int) Math.max(0, Math.min(100, Math.round(score)));

        List<String> concerns = new ArrayList<>();
        List<String> strengths = new ArrayList<>();

        if (spamRatio > 0.05) concerns.add(String.format("%d of %d reviews flagged as suspicious (%.0f%%)",
                spamCount, total, spamRatio * 100));
        if (avgRating < 3.5) concerns.add("Below average customer rating");
        if (variance > 1.5) concerns.add("Ratings are inconsistent across reviewers");
        if (total < 5) concerns.add("Limited review volume");

        if (spamRatio <= 0.05) strengths.add("Review authenticity looks high");
        if (avgRating >= 4.0) strengths.add("Strong average customer rating");
        if (total >= 10) strengths.add("Good review volume");
        if (variance <= 1.0) strengths.add("Consistent opinions across reviewers");

        Map<String, Object> out = new HashMap<>();
        out.put("trustScore", trustScore);
        out.put("analysis", String.format(
                "Based on %d reviews with an average rating of %.1f/5. %d review(s) were flagged as potentially fake. Review quality appears %s.",
                total, avgRating, spamCount,
                spamRatio > 0.15 ? "manipulated" : spamRatio > 0.05 ? "mixed, with some concerns" : "genuine and organic"));
        out.put("concerns", concerns);
        out.put("strengths", strengths);
        return out;
    }

    // --- Sentiment Analysis (Qwen simulator) ---

    public Map<String, Object> analyzeSentiment(String reviewContent) {
        if (!enabled) return null;

        String lower = reviewContent.toLowerCase();
        String sentiment, emotion;
        int score;

        if (lower.contains("love") || lower.contains("excellent") || lower.contains("amazing")
                || lower.contains("perfect") || lower.contains("best") || lower.contains("great")) {
            sentiment = "POSITIVE";
            emotion = "happy";
            score = randomInt(8, 10);
        } else if (lower.contains("bad") || lower.contains("terrible") || lower.contains("awful")
                || lower.contains("worst") || lower.contains("hate") || lower.contains("disappointed")) {
            sentiment = "NEGATIVE";
            emotion = "disappointed";
            score = randomInt(1, 3);
        } else {
            sentiment = "NEUTRAL";
            emotion = "neutral";
            score = randomInt(4, 7);
        }

        return Map.of(
                "sentiment", sentiment,
                "emotion", emotion,
                "score", score,
                "keyPoints", List.of("Customer found product " + sentiment.toLowerCase(),
                        "Emotional tone: " + emotion)
        );
    }

    public Map<String, Object> analyzeSentimentBatch(List<Map<String, Object>> reviews) {
        if (!enabled) return null;
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

    // --- Chat (Qwen simulator) ---

    public String chat(String productName, String question, List<Map<String, Object>> reviews) {
        if (!enabled) return null;

        double avgRating = reviews.stream()
                .mapToDouble(r -> ((Number) r.getOrDefault("rating", 3)).doubleValue())
                .average().orElse(3.0);
        int totalReviews = reviews.size();
        String q = question.toLowerCase();

        if (q.contains("trust") || q.contains("fake") || q.contains("real") || q.contains("authentic")) {
            return String.format(
                    "Based on %d reviews for **%s**, the review patterns look **%s**. Average rating is **%.1f/5**, and the feedback appears **%s**.",
                    totalReviews, productName,
                    avgRating >= 4.0 ? "authentic and organic" : "somewhat mixed",
                    avgRating,
                    avgRating >= 3.5 ? "from genuine customers" : "worth investigating further");
        }
        if (q.contains("problem") || q.contains("issue") || q.contains("complaint") || q.contains("wrong") || q.contains("bad")) {
            if (avgRating >= 4.0) {
                return String.format("Most buyers of **%s** are happy (%.1f/5). A few minor issues are typical, but nothing stands out as a common complaint.",
                        productName, avgRating);
            }
            return String.format("Some customers of **%s** mention concerns (%.1f/5 average). I'd suggest reading the lower-rated reviews for specific issues before buying.",
                    productName, avgRating);
        }
        if (q.contains("price") || q.contains("cost") || q.contains("expensive") || q.contains("cheap") || q.contains("value")) {
            return String.format("If you're weighing price against quality, **%s** has a **%.1f/5** rating from **%d** reviews. That suggests most buyers feel they got good value.",
                    productName, avgRating, totalReviews);
        }
        if (q.contains("recommend") || q.contains("buy") || q.contains("worth") || q.contains("should i") || q.contains("good")) {
            return avgRating >= 3.5
                    ? String.format("Yes, **%s** scores **%.1f/5** across **%d** reviews. Most customers are satisfied, so it's likely a good purchase.",
                        productName, avgRating, totalReviews)
                    : String.format("**%s** has a mixed rating of **%.1f/5**. Read individual reviews carefully to see if the drawbacks matter to you.",
                        productName, avgRating);
        }
        return String.format("Great question about **%s**! Based on **%d** reviews (avg **%.1f/5**), customers generally **%s** this product. Ask about trust, price, or common issues for a deeper answer.",
                productName, totalReviews, avgRating,
                avgRating >= 4.0 ? "love" : avgRating >= 3.0 ? "are satisfied with" : "have mixed feelings about");
    }

    public String generalChat(String question, List<Map<String, Object>> productContext) {
        if (!enabled) return null;

        if (productContext.isEmpty()) {
            return "I'm here to help with your shopping questions! However, I don't see any products in our catalog right now. What can I help you find?";
        }

        String q = question.toLowerCase();

        Map<String, Object> best = productContext.stream()
                .max(Comparator.comparingDouble(a -> ((Number) a.getOrDefault("rating", 0)).doubleValue()))
                .orElse(productContext.get(0));

        Map<String, Object> cheapest = productContext.stream()
                .min(Comparator.comparingDouble(a -> ((Number) a.getOrDefault("price", Double.MAX_VALUE)).doubleValue()))
                .orElse(productContext.get(0));

        // Budget filter
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\$?(\\d{2,4})").matcher(question);
        int budget = -1;
        if (m.find()) {
            try { budget = Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
        }

        if (budget > 0) {
            final int maxPrice = budget;
            List<Map<String, Object>> under = productContext.stream()
                    .filter(p -> ((Number) p.getOrDefault("price", 0)).doubleValue() <= maxPrice)
                    .sorted((a, b) -> Double.compare(((Number) b.get("rating")).doubleValue(), ((Number) a.get("rating")).doubleValue()))
                    .limit(3)
                    .toList();
            if (!under.isEmpty()) {
                StringBuilder sb = new StringBuilder(String.format("Here are the best-rated products under **$%d**:\n\n", maxPrice));
                for (int i = 0; i < under.size(); i++) {
                    Map<String, Object> p = under.get(i);
                    sb.append(String.format("%d. **%s** - $%.2f, %.1f/5 stars, trust %s/100\n",
                            i + 1, p.get("name"), ((Number) p.get("price")).doubleValue(),
                            ((Number) p.get("rating")).doubleValue(), p.get("trustScore")));
                }
                return sb.toString();
            }
            return String.format("I couldn't find any products under **$%d** right now. Try a higher budget or ask for the cheapest option.", budget);
        }

        if (q.contains("cheapest") || q.contains("lowest price") || q.contains("affordable") || q.contains("budget")) {
            return String.format("The most affordable option is **%s** at **$%.2f** (%.1f/5 stars). Let me know if you want a value comparison.",
                    cheapest.get("name"), ((Number) cheapest.get("price")).doubleValue(), ((Number) cheapest.get("rating")).doubleValue());
        }

        if (q.contains("best") || q.contains("top") || q.contains("highest rated") || q.contains("recommend")) {
            return String.format("The highest-rated product is **%s** with **%.1f/5** stars and a trust score of **%s/100**. Want me to compare it with similar products?",
                    best.get("name"), ((Number) best.get("rating")).doubleValue(), best.get("trustScore"));
        }

        if (q.contains("compare") || q.contains("versus") || q.contains(" vs ")) {
            return String.format("I can compare! Our top-rated product is **%s** (%.1f/5) and the most affordable is **%s** ($%.2f). Tell me two product names and I'll compare them directly.",
                    best.get("name"), ((Number) best.get("rating")).doubleValue(),
                    cheapest.get("name"), ((Number) cheapest.get("price")).doubleValue());
        }

        if (q.contains("trust") || q.contains("fake") || q.contains("reliable") || q.contains("scam")) {
            Map<String, Object> mostTrusted = productContext.stream()
                    .max(Comparator.comparingDouble(a -> ((Number) a.getOrDefault("trustScore", 0)).doubleValue()))
                    .orElse(best);
            return String.format("The most trustworthy option is **%s** with a trust score of **%s/100**. Want details on how trust scores work?",
                    mostTrusted.get("name"), mostTrusted.get("trustScore"));
        }

        return String.format("Looking at our catalog of **%d** products, I can help you compare, filter by budget, find the best-rated item, or check trust scores. What would you like to know?",
                productContext.size());
    }

    public Map<String, Object> recommend(String userPreferences, List<Map<String, Object>> products) {
        if (!enabled) return null;

        String prefs = userPreferences == null ? "" : userPreferences.toLowerCase();

        // Score products: keyword match 50%, rating 25%, trust score 25%
        List<ScoredProduct> scored = products.stream()
                .map(p -> new ScoredProduct(p, prefs))
                .sorted(Comparator.comparingDouble((ScoredProduct sp) -> sp.score).reversed())
                .limit(3)
                .toList();

        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (int i = 0; i < scored.size(); i++) {
            ScoredProduct sp = scored.get(i);
            Map<String, Object> rec = new LinkedHashMap<>();
            rec.put("productId", sp.id);
            rec.put("name", sp.name);
            rec.put("price", sp.price);
            rec.put("rating", sp.rating);
            rec.put("trustScore", sp.trustScore);
            rec.put("reason", buildReason(sp, prefs, i + 1));
            rec.put("highlights", buildHighlights(sp));
            recommendations.add(rec);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("recommendations", recommendations);
        result.put("explanation", buildExplanation(prefs, scored, products.size(), userPreferences));
        return result;
    }

    private static class ScoredProduct {
        final long id;
        final String name;
        final double price;
        final double rating;
        final int trustScore;
        final int reviewCount;
        final double score;
        final boolean keywordMatch;

        ScoredProduct(Map<String, Object> p, String prefs) {
            this.id = ((Number) p.getOrDefault("id", 0)).longValue();
            this.name = String.valueOf(p.getOrDefault("name", ""));
            this.price = ((Number) p.getOrDefault("price", 0)).doubleValue();
            this.rating = ((Number) p.getOrDefault("rating", 0)).doubleValue();
            this.trustScore = ((Number) p.getOrDefault("trustScore", 0)).intValue();
            this.reviewCount = ((Number) p.getOrDefault("reviewCount", 0)).intValue();
            this.keywordMatch = matchesKeywords(prefs, name,
                    String.valueOf(p.getOrDefault("description", "")));
            double normalizedRating = Math.min(rating, 5.0) / 5.0;
            double normalizedTrust = Math.min(trustScore, 100.0) / 100.0;
            double keywordScore = keywordMatch ? 1.0 : 0.2;
            this.score = keywordScore * 0.50 + normalizedRating * 0.25 + normalizedTrust * 0.25;
        }
    }

    private static boolean matchesKeywords(String prefs, String name, String description) {
        if (prefs.isBlank()) return true;
        String text = (name + " " + description).toLowerCase();
        Set<String> textTokens = new HashSet<>(Arrays.asList(text.split("[^a-z0-9]+")));
        String[] tokens = prefs.split("\\s+");
        int matches = 0;
        for (String token : tokens) {
            String t = token.replaceAll("[^a-z0-9]", "");
            if (t.length() < 2) continue;
            if (textTokens.contains(t)) matches++;
        }
        return matches > 0;
    }

    private static String buildReason(ScoredProduct sp, String prefs, int rank) {
        StringBuilder sb = new StringBuilder();
        if (rank == 1) sb.append("Top match");
        else if (rank == 2) sb.append("Strong alternative");
        else sb.append("Great value option");
        if (sp.keywordMatch) {
            sb.append(" - aligns directly with your interest in \"").append(prefs).append("\"");
        }
        sb.append(String.format(". Rated %.1f/5 with a trust score of %d/100.", sp.rating, sp.trustScore));
        return sb.toString();
    }

    private static List<String> buildHighlights(ScoredProduct sp) {
        List<String> highlights = new ArrayList<>();
        if (sp.rating >= 4.5) highlights.add("Top-rated pick");
        if (sp.trustScore >= 90) highlights.add("Excellent trust score");
        else if (sp.trustScore >= 75) highlights.add("High trust score");
        if (sp.reviewCount >= 100) highlights.add("Proven by many buyers");
        if (sp.price <= 200) highlights.add("Great value");
        if (highlights.isEmpty()) highlights.add("Well-balanced choice");
        return highlights;
    }

    private static String buildExplanation(String prefs, List<ScoredProduct> picks, int total, String originalPrefs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Based on your request for **\"").append(originalPrefs.isBlank() ? "quality products" : originalPrefs).append("\",** ");
        sb.append("I analyzed **").append(total).append(" products** in our catalog and selected the **top ").append(picks.size()).append(" matches** for you.\n\n");
        sb.append("### How I chose these recommendations\n\n");
        sb.append("- **Relevance:** Keyword match with your description (brand, category, or feature).\n");
        sb.append("- **Customer Rating:** Average star rating from verified buyers.\n");
        sb.append("- **Trust Score:** VeriCart AI's analysis of review authenticity and sentiment.\n\n");
        sb.append("### Top picks\n\n");
        for (int i = 0; i < picks.size(); i++) {
            ScoredProduct sp = picks.get(i);
            sb.append(String.format("%d. **%s** - $%.2f | %.1f/5 stars | Trust %d/100\n",
                    i + 1, sp.name, sp.price, sp.rating, sp.trustScore));
            sb.append("   - ").append(buildReason(sp, prefs, i + 1)).append("\n");
        }
        sb.append("\n*Tip: Click any product card to view details, read reviews, and chat with the AI assistant for a deeper analysis.*");
        return sb.toString();
    }

    // --- Review Summary (Hunyuan simulator) ---

    public Map<String, Object> generateReviewSummary(List<Map<String, Object>> reviews, String productName) {
        if (!enabled) return null;

        double avgRating = reviews.stream()
                .mapToDouble(r -> ((Number) r.getOrDefault("rating", 3)).doubleValue())
                .average().orElse(3.0);
        int count = reviews.size();

        return Map.of(
                "advantages", List.of(
                        "Most customers rate this product highly",
                        "Consistent quality reported across reviews",
                        "Good value for the price point"
                ),
                "disadvantages", avgRating < 4.0
                        ? List.of("Some users reported minor issues", "Occasional quality inconsistency")
                        : List.of("Premium price point", "Limited color/size options"),
                "overallOpinion", String.format("%s receives %.1f/5 from %d reviews. %s",
                        productName, avgRating, count,
                        avgRating >= 4.0 ? "Customers are generally very satisfied."
                                : avgRating >= 3.0 ? "Customer satisfaction is moderate."
                                : "Customer feedback indicates room for improvement."),
                "recommendation", avgRating >= 4.0 ? "BUY" : avgRating >= 3.0 ? "CONSIDER" : "AVOID",
                "recommendationReason", avgRating >= 3.0
                        ? "Overall positive customer sentiment"
                        : "Below average customer reviews suggest caution"
        );
    }

    private int randomInt(int min, int max) {
        return min + new Random().nextInt(max - min + 1);
    }

    // --- Inquiry Auto-Reply (store assistant simulator) ---

    /**
     * Simulates the AI answering a customer's message to the store owner while the
     * seller is unavailable. Uses the product card facts + the conversation history
     * so EVERY message — including follow-ups — gets a proper answer, and matches
     * the customer's language when the question is Chinese.
     */
    public String answerProductQuestion(Map<String, Object> p, String question,
                                        List<Map<String, Object>> history) {
        if (!enabled) return null;
        String q = question.toLowerCase();
        boolean chinese = q.matches(".*[\u4e00-\u9fff].*");

        String head = chinese ? "你好！我是店家的 AI 助手" : "Hi! I'm the store's AI assistant";
        String tail = chinese ? "如果还需要更详细的信息，店主回来后也会亲自回复你。"
                : "If you need more detail, the store owner will personally follow up as soon as they're back.";

        // --- Polite acknowledgments / closers ---
        // Only for a *pure* thank-you. A question that merely starts with "thanks"
        // ("Thanks — can I return it?") must still fall through to a real answer.
        String ack = q.trim();
        boolean noQuestion = !ack.contains("?") && !ack.contains("？");
        if (noQuestion && ack.length() <= 40 && isPureAcknowledgment(ack)) {
            return chinese
                    ? head + "，不客气！有其他问题随时问我，我会一直在线帮你解答。" + tail
                    : head + ", you're welcome! Happy to help — I'm here 24/7 if anything else comes up. " + tail;
        }

        // --- Product deleted from the catalog — never leave the customer unanswered ---
        if (p == null || p.isEmpty()) {
            return chinese
                    ? head + "，很抱歉，这件商品暂时不在架上了。店主会看到你的消息，回来后亲自为你确认。"
                    : head + ", it looks like this item is no longer available in the catalog. The owner will see your message and confirm with you personally as soon as they're back.";
        }

        String name = String.valueOf(p.getOrDefault("name", "this product"));
        double price = ((Number) p.getOrDefault("price", 0)).doubleValue();
        int stock = ((Number) p.getOrDefault("stock", 0)).intValue();
        double rating = ((Number) p.getOrDefault("rating", 0)).doubleValue();
        int trust = ((Number) p.getOrDefault("trustScore", 0)).intValue();

        // --- Stock / availability ---
        if (containsAny(q, "stock", "availab", "inventor", "in store", "backorder", "restock", "有货", "库存", "现货", "缺货")) {
            return stock > 0
                    ? (chinese
                        ? head + "，「" + name + "」目前**有货**，库存 " + stock + " 件，可以立即下单。"
                        : head + ", **" + name + "** is currently **in stock** (" + stock + " units ready to ship).")
                      + " " + tail
                    : (chinese
                        ? head + "，很抱歉，「" + name + "」目前**暂时缺货**，但补货很快，你可以先下单预约。"
                        : head + ", unfortunately **" + name + "** is **out of stock** right now, but restocking is fast — you can still place a pre-order.");
        }

        // --- Shipping / delivery ---
        if (containsAny(q, "ship", "delivery", "deliver", "shipping", "postage", "快递", "物流", "运费", "寄", "发货", "几天", "多久")) {
            return chinese
                    ? head + "，我们一般**48 小时内发货**，国内通常 3–5 个工作日送达（偏远地区略久）。下单后可以在「我的订单」里实时查看物流。"
                    : head + ", we usually **ship within 48 hours**. Delivery typically takes **3–5 business days** (slightly longer for remote areas), and you can track it live in **My Orders** after purchasing.";
        }

        // --- Price / negotiation / discount ---
        if (containsAny(q, "price", "cost", "cheap", "discount", "negotiat", "bargain", "deal", "优惠", "价格", "便宜", "降价", "折扣", "砍价", "能不能便宜")) {
            return chinese
                    ? head + "，「" + name + "」目前售价 **$" + formatPrice(price) + "**，这是 AI 校验过的市场合理价位。店铺不定期有满减活动，加入购物车或关注店铺可以第一时间看到优惠。"
                    : head + ", **" + name + "** is currently priced at **$" + formatPrice(price) + "** — an AI-verified fair market price. The store runs occasional promotions, so adding it to your cart or following the store helps you catch discounts.";
        }

        // --- Authenticity / trust ---
        if (containsAny(q, "authentic", "real", "genuine", "original", "legit", "fake", "正品", "真", "假", "靠谱", "可信")) {
            return chinese
                    ? head + "，请放心，本店商品**支持正品保障**。「" + name + "」的 AI 可信评分为 **" + trust + "/100**，商品页有真实买家评价可供参考。"
                    : head + ", rest assured — every item in this store is **authenticity-guaranteed**. **" + name + "** currently holds a **" + trust + "/100 AI trust score**, backed by real buyer reviews you can read on the product page.";
        }

        // --- Warranty / returns / after-sales ---
        if (containsAny(q, "warranty", "guarantee", "return", "refund", "exchange", "售后", "保修", "退", "换货", "质保")) {
            return chinese
                    ? head + "，本店支持**7 天无理由退换**，并提供官方质保。有任何问题都可以直接联系店主，我们会第一时间处理。"
                    : head + ", we offer **7-day no-questions returns** plus a manufacturer warranty. If anything isn't right, just reach out — we'll sort it out promptly.";
        }

        // --- Variants (color / size / model) ---
        if (containsAny(q, "color", "colour", "size", "variant", "version", "model", "颜色", "尺寸", "版本", "规格")) {
            return chinese
                    ? head + "，「" + name + "」的具体颜色 / 尺寸 / 版本以商品详情页的规格信息为准，下单时也可以选择。如果不确定，店主回来后会帮你确认。"
                    : head + ", the available **colors / sizes / variants** for **" + name + "** are listed in the product specifications — you can pick at checkout. The owner can confirm details when they return.";
        }

        // --- Comparison / vs ---
        if (containsAny(q, "compare", "vs", "difference", "between", "区别", "对比", "哪个好")) {
            return chinese
                    ? head + "，对比不同型号时建议先看商品页的**AI 摘要**和真实评价。我可以帮你比较，你可以到「AI 购物助手」里发起对比分析。"
                    : head + ", for comparisons the best starting point is the **AI review summary** and real buyer feedback on each product page. Ask the **AI Shopping Assistant** for a side-by-side analysis.";
        }

        // --- Quality / is it good / worth buying ---
        if (containsAny(q, "good", "quality", "worth", "recommend", "like it", "好用", "质量", "值得", "推荐", "怎么样", "好不好")) {
            String verdict = rating >= 4.2 ? (chinese ? "好评率很高" : "highly rated")
                    : rating >= 3.5 ? (chinese ? "口碑良好" : "well reviewed")
                    : (chinese ? "评价中等" : "mixed feedback");
            return chinese
                    ? head + "，「" + name + "」买家评分 **" + rating + "/5**，AI 可信评分 **" + trust + "/100**，整体" + verdict + "。建议看看商品页的真实评价再决定。"
                    : head + ", **" + name + "** rates **" + rating + "/5** with an AI trust score of **" + trust + "/100** — overall " + verdict + ". Browse the real reviews on the product page to make sure it fits your needs.";
        }

        // --- General fallback: always answer, in the context of the ongoing chat ---
        String hint = chinese
                ? "我可以继续帮你解答关于库存、发货、价格、正品保障、退换货等问题。"
                : "I can keep helping with stock, shipping, pricing, authenticity, and returns.";

        // Reference the customer's previous question so follow-ups feel continuous
        String topic = "";
        if (history != null && !history.isEmpty()) {
            Map<String, Object> lastUser = null;
            for (int i = history.size() - 1; i >= 0; i--) {
                if ("USER".equals(history.get(i).get("sender"))) { lastUser = history.get(i); break; }
            }
            if (lastUser != null) {
                String prev = String.valueOf(lastUser.getOrDefault("content", "")).trim();
                if (!prev.isEmpty() && !prev.equalsIgnoreCase(question)) {
                    String clipped = prev.length() > 40 ? prev.substring(0, 40) + "…" : prev;
                    topic = chinese
                            ? "结合你之前问的「" + clipped + "」，"
                            : "Tying this back to your earlier question about \"" + clipped + "\", ";
                }
            }
        }

        return chinese
                ? head + "，感谢你对「" + name + "」的关注！" + topic + hint + tail
                : head + ", thanks for your interest in **" + name + "**! " + topic + hint + tail;
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }

    /**
     * True when the message is nothing but a thank-you / confirmation ("ok thanks",
     * "谢谢", "great!"). Used so closers get a short polite reply while a question
     * that merely starts with "thanks" still reaches the real answer logic.
     */
    private static boolean isPureAcknowledgment(String message) {
        String cleaned = message.replaceAll("[^\\p{L}0-9\\s]", " ").trim();
        if (cleaned.isEmpty()) return false;
        String[] tokens = cleaned.split("\\s+");
        if (tokens.length > 5) return false;
        List<String> ack = Arrays.asList(
                "thanks", "thank", "thx", "ty", "ok", "okay", "k", "great", "got", "it",
                "awesome", "perfect", "good", "sure", "bye", "yes", "no", "cheers",
                "appreciate", "appreciated", "nice", "cool", "fine", "alright",
                "好的", "谢谢", "多谢", "感谢", "明白", "收到", "行", "好");
        for (String t : tokens) {
            if (!ack.contains(t)) return false;
        }
        return true;
    }

    private static String formatPrice(double price) {
        return price == Math.floor(price) ? String.format("%.0f", price) : String.format("%.2f", price);
    }
}
