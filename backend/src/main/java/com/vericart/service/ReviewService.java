package com.vericart.service;

import com.vericart.dto.ReviewRequest;
import com.vericart.entity.Product;
import com.vericart.entity.Review;
import com.vericart.entity.User;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.ProductMapper;
import com.vericart.mapper.ReviewMapper;
import com.vericart.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final ProductMapper productMapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;

    @Transactional
    public Review create(Long userId, ReviewRequest request) {
        // Check product exists
        Product product = productMapper.findById(request.getProductId());
        if (product == null) throw new BusinessException(404, "Product not found");

        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(request.getProductId());
        review.setOrderId(request.getOrderId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setImages(request.getImages());
        reviewMapper.insert(review);

        // Update product rating
        updateProductRating(request.getProductId());

        // FR-067 — notify the seller that their product received a review
        if (product.getSellerId() != null && !product.getSellerId().equals(userId)) {
            notificationService.push(
                    product.getSellerId(),
                    "New review received",
                    String.format("Your product \"%s\" received a %d-star review.", product.getName(), request.getRating()),
                    "REVIEW");
        }
        notificationService.push(
                userId,
                "Review submitted",
                String.format("Your review of \"%s\" was published and is being analysed by AI.", product.getName()),
                "REVIEW");

        auditLogService.record(userId, usernameOf(userId), "REVIEW_CREATED", "REVIEW", "REVIEW", review.getId(),
                String.format("Review %d created for product %d (rating %d/5)", review.getId(), request.getProductId(), request.getRating()));
        return review;
    }

    public List<Review> getByProductId(Long productId) {
        return reviewMapper.findByProductId(productId);
    }

    public List<Review> getByUserId(Long userId) {
        return reviewMapper.findByUserId(userId);
    }

    /** FR-034 — edit own review within a configurable window (48h). */
    @Transactional
    public Review update(Long reviewId, Long userId, ReviewRequest request) {
        Review existing = reviewMapper.findById(reviewId);
        if (existing == null) throw new BusinessException(404, "Review not found");
        if (!existing.getUserId().equals(userId)) throw new BusinessException(403, "Access denied");
        if (existing.getCreatedAt() != null
                && existing.getCreatedAt().isBefore(java.time.LocalDateTime.now().minusHours(48))) {
            throw new BusinessException(400, "Reviews can only be edited within 48 hours of posting");
        }
        existing.setRating(request.getRating());
        existing.setContent(request.getContent());
        if (request.getImages() != null) existing.setImages(request.getImages());
        reviewMapper.update(existing);
        updateProductRating(existing.getProductId());

        auditLogService.record(userId, usernameOf(userId), "REVIEW_UPDATED", "REVIEW", "REVIEW", reviewId,
                String.format("Review %d edited (rating -> %d/5)", reviewId, request.getRating()));
        return existing;
    }

    /** FR-037 — user reports a suspicious review; flagged for admin moderation. */
    @Transactional
    public void report(Long reviewId, Long userId) {
        Review review = reviewMapper.findById(reviewId);
        if (review == null) throw new BusinessException(404, "Review not found");
        if (review.getUserId().equals(userId)) throw new BusinessException(400, "You cannot report your own review");
        reviewMapper.flagForModeration(reviewId);

        auditLogService.record(userId, usernameOf(userId), "REVIEW_REPORTED", "REVIEW", "REVIEW", reviewId,
                "User reported review " + reviewId + " as suspicious");
    }

    /**
     * Trust metrics powering the "Trusted Rating" explainable-AI feature:
     * raw rating vs rating after AI removes suspicious reviews.
     */
    public Map<String, Object> getTrustMetrics(Long productId) {
        int total = reviewMapper.countByProduct(productId);
        int flagged = reviewMapper.countFlaggedByProduct(productId);
        BigDecimal raw = reviewMapper.avgRatingByProduct(productId);
        BigDecimal trusted = reviewMapper.avgTrustedRating(productId);
        if (raw == null) raw = BigDecimal.ZERO;
        if (trusted == null) trusted = raw;

        Map<String, Object> m = new HashMap<>();
        m.put("totalReviews", total);
        m.put("flaggedReviews", flagged);
        m.put("rawRating", raw);
        m.put("trustedRating", trusted);
        m.put("ratingDelta", raw.subtract(trusted));
        return m;
    }

    /** True when the user already reviewed this product (prevents duplicates). */
    public boolean hasUserReviewed(Long userId, Long productId) {
        return reviewMapper.countByUserAndProduct(userId, productId) > 0;
    }

    @Transactional
    public void delete(Long reviewId, Long userId) {
        Review review = reviewMapper.findById(reviewId);
        if (review == null) throw new BusinessException(404, "Review not found");
        if (!review.getUserId().equals(userId)) throw new BusinessException(403, "Access denied");

        reviewMapper.softDelete(reviewId);
        updateProductRating(review.getProductId());

        auditLogService.record(userId, usernameOf(userId), "REVIEW_DELETED", "REVIEW", "REVIEW", reviewId,
                "User deleted review " + reviewId);
    }

    private String usernameOf(Long userId) {
        if (userId == null) return null;
        User u = userMapper.findById(userId);
        return u != null ? u.getUsername() : null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAiFields(Long reviewId, String sentiment, String emotion,
                               BigDecimal fakeProbability, String fakeReason, Integer isFlagged) {
        Review review = new Review();
        review.setId(reviewId);
        review.setSentiment(sentiment);
        review.setEmotion(emotion);
        review.setFakeProbability(fakeProbability);
        review.setFakeReason(fakeReason);
        review.setIsFlagged(isFlagged);
        reviewMapper.updateAiAnalysis(review);
    }

    public List<Review> findNeedingAnalysis(int limit) {
        return reviewMapper.findReviewsNeedingAnalysis(limit);
    }

    public List<Review> findFlaggedReviews() {
        return reviewMapper.findFlaggedReviews();
    }

    private void updateProductRating(Long productId) {
        int count = reviewMapper.countByProduct(productId);
        BigDecimal avgRating = reviewMapper.avgRatingByProduct(productId);
        if (avgRating == null) avgRating = BigDecimal.ZERO;

        Product product = productMapper.findById(productId);
        if (product != null) {
            product.setRating(avgRating);
            product.setReviewCount(count);
            productMapper.update(product);
        }
    }
}
