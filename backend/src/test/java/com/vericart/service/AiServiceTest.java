package com.vericart.service;

import com.vericart.ai.AiGateway;
import com.vericart.ai.AiMockService;
import com.vericart.dto.AiChatResponse;
import com.vericart.entity.Product;
import com.vericart.entity.Review;
import com.vericart.mapper.ProductMapper;
import com.vericart.mapper.ReviewMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiService Unit Tests")
class AiServiceTest {

    @Mock private AiGateway aiGateway;
    @Mock private AiMockService mockService; // conversational safety-net fallback
    @Mock private ReviewMapper reviewMapper;
    @Mock private ProductMapper productMapper;
    @Mock private ReviewService reviewService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AiService aiService;

    private Product product;
    private Review review;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Phone");
        product.setPrice(new BigDecimal("699.00"));

        review = new Review();
        review.setId(1L);
        review.setUserId(2L);
        review.setProductId(1L);
        review.setRating(5);
        review.setContent("Amazing phone, best purchase ever!");
    }

    @Test
    @DisplayName("Should return message when no reviews exist")
    void shouldHandleNoReviews() {
        when(productMapper.findById(1L)).thenReturn(product);
        when(reviewMapper.findByProductId(1L)).thenReturn(List.of());

        Map<String, Object> result = aiService.analyzeProduct(1L);

        assertEquals("No reviews to analyze yet", result.get("message"));
        assertEquals(1L, result.get("productId"));
    }

    @Test
    @DisplayName("Should throw when product not found")
    void shouldThrowProductNotFound() {
        when(productMapper.findById(999L)).thenReturn(null);

        assertThrows(com.vericart.exception.BusinessException.class,
                () -> aiService.analyzeProduct(999L));
    }

    @Test
    @DisplayName("Should run full analysis pipeline")
    void shouldRunAnalysisPipeline() {
        when(productMapper.findById(1L)).thenReturn(product);
        when(reviewMapper.findByProductId(1L)).thenReturn(List.of(review));

        // Sentiment
        when(aiGateway.analyzeSentiment(review.getContent()))
                .thenReturn(Map.of("sentiment", "POSITIVE", "emotion", "excited"));

        // Fake detection
        when(aiGateway.detectFakeReview(review.getContent(), product.getName()))
                .thenReturn(Map.of("probability", 0.02, "isSuspicious", false, "reasons", "Genuine"));

        // Trust score
        when(aiGateway.generateTrustScore(eq("Test Phone"), anyList()))
                .thenReturn(Map.of("trustScore", 88, "trustLevel", "Excellent"));

        // Summary
        when(aiGateway.generateReviewSummary(anyList(), eq("Test Phone")))
                .thenReturn(Map.of("advantages", List.of("Great quality"), "disadvantages", List.of(),
                        "overallOpinion", "Excellent", "recommendation", "Highly Recommended"));

        Map<String, Object> result = aiService.analyzeProduct(1L);

        assertNotNull(result);
        assertEquals(88, result.get("trustScore"));
        assertEquals("Excellent", result.get("trustLevel"));
        assertEquals(1, result.get("totalReviews"));

        verify(reviewService).updateAiFields(eq(1L), eq("POSITIVE"), eq("excited"),
                any(BigDecimal.class), any(), eq(0));
    }

    @Test
    @DisplayName("Should handle AI partial failures gracefully")
    void shouldHandlePartialFailure() {
        when(productMapper.findById(1L)).thenReturn(product);
        when(reviewMapper.findByProductId(1L)).thenReturn(List.of(review));

        // Sentiment fails → the review is skipped inside the per-review try/catch,
        // so fake detection is not reached for it (unused stubbing would be a Mockito error).
        when(aiGateway.analyzeSentiment(anyString()))
                .thenThrow(new RuntimeException("Qwen API error"));
        when(aiGateway.generateTrustScore(anyString(), anyList()))
                .thenReturn(Map.of("trustScore", 80, "trustLevel", "High"));
        when(aiGateway.generateReviewSummary(anyList(), anyString()))
                .thenReturn(Map.of("advantages", List.of("Good")));

        // Should not throw, should continue with what it can
        Map<String, Object> result = aiService.analyzeProduct(1L);

        assertNotNull(result);
        assertEquals(80, result.get("trustScore"));
    }

    @Test
    @DisplayName("Should handle chat request for product")
    void shouldChat() {
        when(productMapper.findById(1L)).thenReturn(product);
        when(reviewMapper.findByProductId(1L)).thenReturn(List.of());
        when(aiGateway.chat(eq(1L), eq("Test Phone"), eq("Is it good?"), anyList(), anyList()))
                .thenReturn(new AiChatResponse("Yes, highly recommended!", "qwen-plus", 1L));

        com.vericart.dto.AiChatRequest request = new com.vericart.dto.AiChatRequest();
        request.setProductId(1L);
        request.setQuestion("Is it good?");

        AiChatResponse response = aiService.chat(1L, request);

        assertEquals("Yes, highly recommended!", response.getAnswer());
        assertEquals("qwen-plus", response.getModel());
    }

    @Test
    @DisplayName("Should fall back to conversational reply when live LLM fails (never goes silent)")
    void shouldFallbackOnChatFailure() {
        when(productMapper.findById(1L)).thenReturn(product);
        when(reviewMapper.findByProductId(1L)).thenReturn(List.of());
        when(aiGateway.chat(eq(1L), eq("Test Phone"), eq("hi"), anyList(), anyList()))
                .thenThrow(new RuntimeException("DeepSeek API down"));
        when(mockService.chat(eq("Test Phone"), eq("hi"), anyList()))
                .thenReturn("Hi! I see you're looking at Test Phone 🙂");

        com.vericart.dto.AiChatRequest request = new com.vericart.dto.AiChatRequest();
        request.setProductId(1L);
        request.setQuestion("hi");

        AiChatResponse response = aiService.chat(1L, request);

        assertEquals("Hi! I see you're looking at Test Phone 🙂", response.getAnswer());
        assertEquals("fallback", response.getModel());
        assertEquals(1L, response.getProductId());
    }

    @Test
    @DisplayName("General chat should fall back to conversational reply on LLM failure (e.g., greeting)")
    void shouldFallbackOnGeneralChatFailure() {
        List<Map<String, Object>> ctx = List.of(
                Map.of("id", 1, "name", "Phone X", "price", 699.0, "rating", 4.7, "trustScore", 92)
        );
        when(productMapper.findAll(anyInt(), anyInt(), any(), any(), any())).thenReturn(java.util.Collections.emptyList());
        when(aiGateway.generalChat(eq("hi"), anyList(), anyList()))
                .thenThrow(new RuntimeException("DeepSeek API down"));
        when(mockService.generalChat(eq("hi"), anyList()))
                .thenReturn("Hi there! 👋 I'm VeriCart AI, your shopping assistant.");

        AiChatResponse response = aiService.generalChat(1L, "hi", null);

        assertEquals("Hi there! 👋 I'm VeriCart AI, your shopping assistant.", response.getAnswer());
        assertEquals("fallback", response.getModel());
        assertNull(response.getProductId());
    }
}
