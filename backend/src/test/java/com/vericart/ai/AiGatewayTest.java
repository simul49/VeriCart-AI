package com.vericart.ai;

import com.vericart.dto.AiChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiGateway Unit Tests")
class AiGatewayTest {

    @Mock private DeepSeekClient deepSeekClient;
    @Mock private QwenClient qwenClient;
    @Mock private HunyuanClient hunyuanClient;
    @Mock private KimiClient kimiClient;

    @InjectMocks
    private AiGateway aiGateway;

    private List<Map<String, Object>> reviewData;

    @BeforeEach
    void setUp() {
        reviewData = List.of(
                Map.of("id", 1, "rating", 4, "content", "Great product!"),
                Map.of("id", 2, "rating", 5, "content", "Excellent quality")
        );
    }

    @Test
    @DisplayName("Should route fake review detection to DeepSeek")
    void shouldRouteFakeDetectionToDeepSeek() {
        Map<String, Object> expected = Map.of("probability", 0.1, "isSuspicious", false);
        when(deepSeekClient.detectFakeReview("Great!", "TestProduct")).thenReturn(expected);

        Map<String, Object> result = aiGateway.detectFakeReview("Great!", "TestProduct");
        assertEquals(expected, result);
        verify(deepSeekClient).detectFakeReview("Great!", "TestProduct");
    }

    @Test
    @DisplayName("Should route sentiment analysis to Qwen")
    void shouldRouteSentimentToQwen() {
        Map<String, Object> expected = Map.of("sentiment", "POSITIVE", "emotion", "happy");
        when(qwenClient.analyzeSentiment("Love it!")).thenReturn(expected);

        Map<String, Object> result = aiGateway.analyzeSentiment("Love it!");
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should route review summary to Hunyuan")
    void shouldRouteSummaryToHunyuan() {
        Map<String, Object> expected = Map.of("advantages", List.of("Good"), "disadvantages", List.of());
        when(hunyuanClient.generateReviewSummary(reviewData, "Phone")).thenReturn(expected);

        Map<String, Object> result = aiGateway.generateReviewSummary(reviewData, "Phone");
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Should generate trust score with multi-model orchestration")
    void shouldGenerateTrustScore() {
        when(deepSeekClient.analyzeTrust(eq("Phone"), any()))
                .thenReturn(Map.of("trustScore", 85));
        when(qwenClient.analyzeSentimentBatch(any()))
                .thenReturn(Map.of("positiveRatio", 0.9));

        Map<String, Object> result = aiGateway.generateTrustScore("Phone", reviewData);

        assertNotNull(result);
        assertTrue(result.containsKey("trustScore"));
        assertTrue(result.containsKey("trustLevel"));
        assertEquals(2, result.get("totalReviews"));
    }

    @Test
    @DisplayName("Should survive DeepSeek failure and use fallback score")
    void shouldFallbackWhenDeepSeekFails() {
        when(deepSeekClient.analyzeTrust(any(), any()))
                .thenThrow(new RuntimeException("API down"));
        when(qwenClient.analyzeSentimentBatch(any()))
                .thenReturn(Map.of("positiveRatio", 0.5));

        Map<String, Object> result = aiGateway.generateTrustScore("Phone", reviewData);

        assertNotNull(result);
        int score = (int) result.get("trustScore");
        // base 70 * 0.6 + 0.5 * 40 = 42 + 20 = 62, but with < 3 reviews cap = min(62, 60) = 60
        assertEquals(60, score);
    }

    @Test
    @DisplayName("Should cap trust score for products with few reviews")
    void shouldCapLowReviewCount() {
        List<Map<String, Object>> singleReview = List.of(
                Map.of("id", 1, "rating", 5, "content", "Amazing!")
        );
        when(deepSeekClient.analyzeTrust(any(), any()))
                .thenReturn(Map.of("trustScore", 95));
        when(qwenClient.analyzeSentimentBatch(any()))
                .thenReturn(Map.of("positiveRatio", 1.0));

        Map<String, Object> result = aiGateway.generateTrustScore("Phone", singleReview);

        int score = (int) result.get("trustScore");
        assertTrue(score <= 60, "Low review count should cap score at 60");
    }

    @Test
    @DisplayName("Should compute trust level labels correctly")
    void shouldComputeTrustLevel() {
        // 3+ reviews so the low-volume cap (< 3 reviews → max 60) does not apply
        List<Map<String, Object>> threeReviews = List.of(
                Map.of("id", 1, "rating", 5, "content", "Great product!"),
                Map.of("id", 2, "rating", 4, "content", "Good value"),
                Map.of("id", 3, "rating", 5, "content", "Excellent quality")
        );
        when(deepSeekClient.analyzeTrust(any(), any()))
                .thenReturn(Map.of("trustScore", 90));
        when(qwenClient.analyzeSentimentBatch(any()))
                .thenReturn(null); // Qwen fails

        Map<String, Object> result = aiGateway.generateTrustScore("Phone", threeReviews);

        assertEquals("Excellent", result.get("trustLevel"));
    }

    @Test
    @DisplayName("Chat should route to Kimi when configured")
    void shouldRouteChatToKimi() {
        when(kimiClient.isConfigured()).thenReturn(true);
        when(kimiClient.chat(anyString(), anyString(), any(), any()))
                .thenReturn("Here's my recommendation...");

        AiChatResponse response = aiGateway.chat(1L, "Phone", "Should I buy?", reviewData, null);

        assertEquals("Here's my recommendation...", response.getAnswer());
        assertEquals("kimi", response.getModel());
        verify(kimiClient).chat(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Chat should use DeepSeek directly when Kimi is not configured")
    void shouldUseDeepSeekWhenKimiUnconfigured() {
        when(kimiClient.isConfigured()).thenReturn(false);
        when(deepSeekClient.chat(anyString(), anyString(), any(), any()))
                .thenReturn("Answer from DeepSeek");

        AiChatResponse response = aiGateway.chat(1L, "Phone", "Should I buy?", reviewData, null);

        assertEquals("Answer from DeepSeek", response.getAnswer());
        assertEquals("deepseek", response.getModel());
        verify(kimiClient, never()).chat(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Chat should fall back to DeepSeek when Kimi throws")
    void shouldFallbackChatToDeepSeek() {
        when(kimiClient.isConfigured()).thenReturn(true);
        when(kimiClient.chat(anyString(), anyString(), any(), any()))
                .thenThrow(new RuntimeException("Kimi down"));
        when(deepSeekClient.chat(anyString(), anyString(), any(), any()))
                .thenReturn("Fallback answer from DeepSeek");

        AiChatResponse response = aiGateway.chat(1L, "Phone", "Should I buy?", reviewData, null);

        assertEquals("Fallback answer from DeepSeek", response.getAnswer());
        assertEquals("deepseek", response.getModel());
    }

    @Test
    @DisplayName("General chat should route to Kimi when configured")
    void shouldRouteGeneralChatToKimi() {
        when(kimiClient.isConfigured()).thenReturn(true);
        when(kimiClient.generalChat(anyString(), any(), any()))
                .thenReturn("Let me help you find products...");

        AiChatResponse response = aiGateway.generalChat("cheap laptops", List.of(), null);

        assertEquals("kimi", response.getModel());
        assertNull(response.getProductId());
        verify(kimiClient).generalChat(anyString(), any());
    }

    @Test
    @DisplayName("General chat should use DeepSeek directly when Kimi is not configured")
    void shouldUseDeepSeekGeneralWhenKimiUnconfigured() {
        when(kimiClient.isConfigured()).thenReturn(false);
        when(deepSeekClient.generalChat(anyString(), any(), any()))
                .thenReturn("General answer from DeepSeek");

        AiChatResponse response = aiGateway.generalChat("cheap laptops", List.of(), null);

        assertEquals("deepseek", response.getModel());
        assertNull(response.getProductId());
        verify(kimiClient, never()).generalChat(anyString(), any());
    }

    @Test
    @DisplayName("General chat should fall back to DeepSeek when Kimi throws")
    void shouldFallbackGeneralChatToDeepSeek() {
        when(kimiClient.isConfigured()).thenReturn(true);
        when(kimiClient.generalChat(anyString(), any(), any()))
                .thenThrow(new RuntimeException("Kimi down"));
        when(deepSeekClient.generalChat(anyString(), any(), any()))
                .thenReturn("Fallback general answer from DeepSeek");

        AiChatResponse response = aiGateway.generalChat("cheap laptops", List.of(), null);

        assertEquals("deepseek", response.getModel());
        assertNull(response.getProductId());
    }
}
