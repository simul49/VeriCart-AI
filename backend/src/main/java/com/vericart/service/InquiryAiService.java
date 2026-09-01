package com.vericart.service;

import com.vericart.ai.AiGateway;
import com.vericart.entity.InquiryMessage;
import com.vericart.entity.Product;
import com.vericart.mapper.InquiryMapper;
import com.vericart.mapper.InquiryMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI auto-reply for product inquiries — answers the customer immediately (for
 * every turn of the conversation) while the store owner is unavailable, using
 * the product card as context. The owner can join the thread anytime with a
 * personal reply.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryAiService {

    private final AiGateway aiGateway;
    private final InquiryMapper inquiryMapper;
    private final InquiryMessageMapper inquiryMessageMapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    /** Fire-and-forget — never blocks the send-message flow. */
    @Async
    public void autoReplyAsync(Long inquiryId, Long customerId, String productName,
                               Map<String, Object> productInfo, String question) {
        try {
            // Conversation history (previous turns only — the current question is
            // passed separately) so the AI can answer every follow-up in context.
            List<InquiryMessage> turns = inquiryMessageMapper.findByInquiry(inquiryId);
            List<Map<String, Object>> history = new ArrayList<>();
            for (int i = 0; i < turns.size() - 1; i++) {
                InquiryMessage m = turns.get(i);
                history.add(Map.of("sender", m.getSender(), "content", m.getContent()));
            }

            String answer = aiGateway.answerInquiry(productInfo, question, history);
            if (answer == null || answer.isBlank()) {
                log.warn("AI inquiry auto-reply returned empty for inquiry {}", inquiryId);
                return;
            }
            String trimmed = answer.trim();
            if (trimmed.length() > 2000) trimmed = trimmed.substring(0, 2000);

            InquiryMessage turn = new InquiryMessage();
            turn.setInquiryId(inquiryId);
            turn.setSender("AI");
            turn.setContent(trimmed);
            inquiryMessageMapper.insert(turn);
            inquiryMapper.markReplied(inquiryId);

            notificationService.push(customerId,
                    "🤖 AI answered your question",
                    "While the owner is away, our AI replied about " + productName,
                    "INQUIRY");
            log.info("AI auto-replied to inquiry {} (product {})", inquiryId, productName);
        } catch (Exception e) {
            log.warn("AI inquiry auto-reply failed for inquiry {}: {}", inquiryId, e.getMessage());
            auditLogService.recordError(null, null, "AI_ANALYSIS_FAILED",
                    "AI auto-reply failed for inquiry " + inquiryId + ": " + e.getMessage());
        }
    }

    /** Builds the product-context map the AI uses to answer. */
    public static Map<String, Object> buildProductInfo(Product product) {
        Map<String, Object> info = new HashMap<>();
        info.put("name", product.getName());
        info.put("brand", product.getBrand());
        info.put("price", product.getPrice());
        info.put("stock", product.getStock());
        info.put("rating", product.getRating());
        info.put("trustScore", product.getTrustScore());
        info.put("description", product.getDescription());
        info.put("aiSummary", product.getAiSummary());
        return info;
    }
}
