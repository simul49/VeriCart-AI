package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.dto.ReviewRequest;
import com.vericart.entity.Review;
import com.vericart.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<Review> create(@Valid @RequestBody ReviewRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(reviewService.create(userId, request));
    }

    @GetMapping("/product/{productId}")
    public Result<List<Review>> byProduct(@PathVariable Long productId) {
        return Result.success(reviewService.getByProductId(productId));
    }

    /** Trust metrics — raw rating vs AI-trusted rating after removing suspicious reviews. */
    @GetMapping("/product/{productId}/trust")
    public Result<Map<String, Object>> trustMetrics(@PathVariable Long productId) {
        return Result.success(reviewService.getTrustMetrics(productId));
    }

    /** Current user's reviews (used by "My Reviews" tab). */
    @GetMapping("/my")
    public Result<List<Review>> myReviews(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(reviewService.getByUserId(userId));
    }

    /** Whether the current user already reviewed this product. */
    @GetMapping("/product/{productId}/mine")
    public Result<Map<String, Object>> hasReviewed(@PathVariable Long productId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        List<Review> mine = reviewService.getByUserId(userId).stream()
                .filter(r -> r.getProductId().equals(productId))
                .toList();
        Map<String, Object> m = new HashMap<>();
        m.put("hasReviewed", !mine.isEmpty());
        m.put("review", mine.isEmpty() ? null : mine.get(0));
        return Result.success(m);
    }

    /** FR-034 — edit own review. */
    @PutMapping("/{id}")
    public Result<Review> update(@PathVariable Long id,
                                 @Valid @RequestBody ReviewRequest request,
                                 Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(reviewService.update(id, userId, request));
    }

    /** FR-037 — report a suspicious review. */
    @PostMapping("/{id}/report")
    public Result<Void> report(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        reviewService.report(id, userId);
        return Result.success("Review reported for moderation", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        reviewService.delete(id, userId);
        return Result.success(null);
    }
}
