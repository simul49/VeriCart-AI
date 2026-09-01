package com.vericart.service;

import com.vericart.entity.Inquiry;
import com.vericart.entity.InquiryMessage;
import com.vericart.entity.Product;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.InquiryMapper;
import com.vericart.mapper.InquiryMessageMapper;
import com.vericart.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Product inquiries — a customer and the store (owner + AI assistant) chat in a
 * thread. Every customer message gets an instant AI auto-reply while the owner
 * is unavailable; the owner can join the conversation anytime with a personal
 * reply.
 */
@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryMapper inquiryMapper;
    private final InquiryMessageMapper inquiryMessageMapper;
    private final ProductMapper productMapper;
    private final NotificationService notificationService;
    private final InquiryAiService inquiryAiService;

    /**
     * Customer starts a new conversation about a product. The owner gets a
     * notification and the AI assistant answers immediately.
     */
    public Inquiry send(Long userId, Long productId, String message) {
        if (productId == null) throw new BusinessException(400, "Product is required");
        checkMessage(message);

        Product product = productMapper.findById(productId);
        if (product == null) throw new BusinessException(404, "Product not found");

        Inquiry inquiry = new Inquiry();
        inquiry.setProductId(productId);
        inquiry.setUserId(userId);
        inquiry.setSellerId(product.getSellerId() != null ? product.getSellerId() : 1L);
        inquiry.setMessage(message.trim());
        inquiry.setStatus("OPEN");
        inquiry.setIsRead(0);
        inquiryMapper.insert(inquiry);

        insertTurn(inquiry.getId(), "USER", message.trim());

        notificationService.push(inquiry.getSellerId(),
                "📩 New product inquiry",
                product.getName() + " — " + message.trim(),
                "INQUIRY");

        aiReply(inquiry.getId(), userId, product, message.trim());
        return inquiry;
    }

    /**
     * Customer sends a follow-up in an existing conversation — the thread keeps
     * growing and the AI answers again.
     */
    public Inquiry sendFollowUp(Long inquiryId, Long userId, String message) {
        if (inquiryId == null) throw new BusinessException(400, "Inquiry is required");
        checkMessage(message);

        Inquiry inquiry = inquiryMapper.findById(inquiryId);
        if (inquiry == null) throw new BusinessException(404, "Conversation not found");
        if (!inquiry.getUserId().equals(userId)) throw new BusinessException(403, "Not your conversation");

        insertTurn(inquiryId, "USER", message.trim());
        inquiryMapper.markNewMessage(inquiryId);

        Product product = productMapper.findById(inquiry.getProductId());
        notificationService.push(inquiry.getSellerId(),
                "📩 New message in conversation",
                (product != null ? product.getName() : "Product") + " — " + message.trim(),
                "INQUIRY");

        aiReply(inquiryId, userId, product, message.trim());
        return inquiryMapper.findById(inquiryId);
    }

    private void checkMessage(String message) {
        if (!StringUtils.hasText(message)) throw new BusinessException(400, "Message cannot be empty");
        if (message.trim().length() > 2000) throw new BusinessException(400, "Message is too long (max 2000 chars)");
    }

    private void insertTurn(Long inquiryId, String sender, String content) {
        InquiryMessage turn = new InquiryMessage();
        turn.setInquiryId(inquiryId);
        turn.setSender(sender);
        turn.setContent(content);
        inquiryMessageMapper.insert(turn);
    }

    private void aiReply(Long inquiryId, Long customerId, Product product, String question) {
        inquiryAiService.autoReplyAsync(inquiryId, customerId,
                product != null ? product.getName() : "your product",
                product != null ? InquiryAiService.buildProductInfo(product) : null,
                question);
    }

    /** Customer's own conversations, each with the full chat history. */
    public List<Map<String, Object>> myMessages(Long userId) {
        return attachMessages(inquiryMapper.findByUser(userId));
    }

    /** Store owner's inbox, each with the full chat history. */
    public List<Map<String, Object>> sellerInbox(Long sellerId) {
        return attachMessages(inquiryMapper.findBySeller(sellerId));
    }

    private List<Map<String, Object>> attachMessages(List<Map<String, Object>> threads) {
        for (Map<String, Object> t : threads) {
            Long id = ((Number) t.get("id")).longValue();
            List<Map<String, Object>> turns = new ArrayList<>();
            for (InquiryMessage m : inquiryMessageMapper.findByInquiry(id)) {
                turns.add(Map.of(
                        "id", m.getId(),
                        "sender", m.getSender(),
                        "content", m.getContent(),
                        "isRead", m.getIsRead(),
                        "createdAt", m.getCreatedAt()));
            }
            t.put("messages", turns);
        }
        return threads;
    }

    public int sellerUnreadCount(Long sellerId) {
        return inquiryMapper.countUnreadBySeller(sellerId);
    }

    /** Store owner replies to a customer — the reply is appended to the conversation. */
    public Inquiry reply(Long inquiryId, Long sellerId, String reply) {
        if (!StringUtils.hasText(reply)) throw new BusinessException(400, "Reply cannot be empty");
        Inquiry inquiry = inquiryMapper.findById(inquiryId);
        if (inquiry == null) throw new BusinessException(404, "Conversation not found");
        if (!inquiry.getSellerId().equals(sellerId)) throw new BusinessException(403, "Not your conversation");

        insertTurn(inquiryId, "SELLER", reply.trim());
        inquiryMapper.markReplied(inquiryId);
        inquiryMapper.markRead(inquiryId, sellerId);
        inquiryMessageMapper.markRead(inquiryId, "USER");

        notificationService.push(inquiry.getUserId(),
                "✅ The store replied",
                "The store owner replied in your conversation about inquiry #" + inquiryId,
                "INQUIRY");
        return inquiryMapper.findById(inquiryId);
    }

    /** Mark a conversation as read for the store owner. */
    public void markRead(Long inquiryId, Long sellerId) {
        Inquiry inquiry = inquiryMapper.findById(inquiryId);
        if (inquiry == null) throw new BusinessException(404, "Conversation not found");
        if (!inquiry.getSellerId().equals(sellerId)) throw new BusinessException(403, "Not your conversation");
        inquiryMapper.markRead(inquiryId, sellerId);
        inquiryMessageMapper.markRead(inquiryId, "USER");
    }
}
