package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.dto.ReviewRequest;
import com.vericart.entity.Review;
import com.vericart.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        reviewService.delete(id, userId);
        return Result.success(null);
    }
}
