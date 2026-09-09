package com.vericart.scheduler;

import com.vericart.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Auto-verifies new customer reviews.
 *
 * When a user posts a review it is published immediately but its AI verdict
 * (sentiment + fake-review probability) starts as "Pending AI". This scheduler
 * kicks off {@link AiService#batchAnalyzePending()} every ~30 seconds so any
 * pending review is automatically verified/approved shortly after submission,
 * without anyone having to click "Refresh Analysis".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiScheduler {

    private final AiService aiService;

    @Scheduled(initialDelay = 10000, fixedDelay = 30000)
    public void autoVerifyReviews() {
        try {
            aiService.batchAnalyzePending();
        } catch (Exception e) {
            log.warn("[AI Scheduler] batch analysis kick-off failed: {}", e.getMessage());
        }
    }
}
