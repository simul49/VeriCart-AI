package com.vericart.service;

import com.vericart.dto.ReviewRequest;
import com.vericart.entity.Product;
import com.vericart.entity.Review;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.ProductMapper;
import com.vericart.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final ProductMapper productMapper;

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

        return review;
    }

    public List<Review> getByProductId(Long productId) {
        return reviewMapper.findByProductId(productId);
    }

    @Transactional
    public void delete(Long reviewId, Long userId) {
        Review review = reviewMapper.findById(reviewId);
        if (review == null) throw new BusinessException(404, "Review not found");
        if (!review.getUserId().equals(userId)) throw new BusinessException(403, "Access denied");

        reviewMapper.softDelete(reviewId);
        updateProductRating(review.getProductId());
    }

    @Transactional
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
