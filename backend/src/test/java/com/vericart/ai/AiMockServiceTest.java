package com.vericart.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the conversational behavior of the AI safety-net:
 *   • Greetings ("hi", "hello", "hey") must NOT return the old canned catalog line.
 *   • Greeting replies must vary across calls (templates, not a single string).
 *   • Product-scoped greetings reference the product name.
 *   • These methods work even when `enabled=false` (used as last-resort fallback).
 */
class AiMockServiceTest {

    private AiMockService mock;
    private final List<Map<String, Object>> ctx = List.of(
            Map.of("name", "Phone X", "price", 699.0, "rating", 4.7, "trustScore", 92)
    );
    private final List<Map<String, Object>> reviews = List.of(
            Map.of("rating", 5, "content", "Great phone!")
    );

    @BeforeEach
    void setUp() {
        // enabled=false to prove chat methods are NOT gated (they're the safety net).
        mock = new AiMockService(false);
    }

    @Test
    @DisplayName("Greeting 'hi' returns a varied greeting — not the canned catalog template")
    void greetingHiReturnsVariedGreeting() {
        String reply = mock.generalChat("hi", ctx);
        assertFalse(reply.toLowerCase().contains("looking at our catalog"),
                "Greeting must not return the old canned catalog line, got: " + reply);
        String low = reply.toLowerCase();
        assertTrue(low.contains("hi") || low.contains("hello") || low.contains("hey")
                        || low.contains("vericart ai"),
                "Reply should sound like a greeting, got: " + reply);
    }

    @Test
    @DisplayName("Greeting 'hello' returns a greeting, not the canned catalog template")
    void greetingHelloReturnsGreeting() {
        String reply = mock.generalChat("hello", ctx);
        assertFalse(reply.toLowerCase().contains("looking at our catalog"),
                "Greeting must not return the old canned catalog line, got: " + reply);
    }

    @Test
    @DisplayName("Greeting replies vary across calls (not a single canned string)")
    void greetingRepliesVary() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 60; i++) seen.add(mock.generalChat("hi", ctx));
        assertTrue(seen.size() >= 2,
                "Greeting should vary across calls; saw " + seen.size() + " distinct replies");
    }

    @Test
    @DisplayName("Product-scoped greeting references the product name")
    void productScopedGreetingReferencesProduct() {
        String reply = mock.chat("Phone X", "hi", reviews);
        assertTrue(reply.contains("Phone X"),
                "Product-scoped greeting should mention the product, got: " + reply);
        assertFalse(reply.toLowerCase().contains("looking at our catalog"),
                "Must not return the old canned line, got: " + reply);
    }

    @Test
    @DisplayName("Real questions are still answered (not treated as social)")
    void realQuestionStillAnswered() {
        String reply = mock.generalChat("cheapest laptop under 500", ctx);
        // A real question should not be greeted — it should reference products/prices.
        String low = reply.toLowerCase();
        assertFalse(low.startsWith("hi") && low.length() < 60,
                "Real question should not be answered with a short greeting, got: " + reply);
    }

    @Test
    @DisplayName("Closer 'thanks' returns a polite closer, not the catalog template")
    void closerThanksReturnsPoliteReply() {
        String reply = mock.generalChat("thanks", ctx);
        assertFalse(reply.toLowerCase().contains("looking at our catalog"),
                "Closer must not return canned catalog line, got: " + reply);
        String low = reply.toLowerCase();
        assertTrue(low.contains("welcome") || low.contains("happy") || low.contains("anytime")
                        || low.contains("help"),
                "Closer should sound polite, got: " + reply);
    }
}