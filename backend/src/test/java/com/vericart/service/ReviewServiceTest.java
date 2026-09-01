package com.vericart.service;

import com.vericart.entity.Review;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.ProductMapper;
import com.vericart.mapper.ReviewMapper;
import com.vericart.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTest {

    @Mock private ReviewMapper reviewMapper;
    @Mock private ProductMapper productMapper;
    @Mock private NotificationService notificationService;
    @Mock private AuditLogService auditLogService;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;
    private com.vericart.entity.Product product;

    @BeforeEach
    void setUp() {
        review = new Review();
        review.setId(1L);
        review.setUserId(1L);
        review.setProductId(10L);
        review.setRating(4);
        review.setContent("Great product!");

        product = new com.vericart.entity.Product();
        product.setId(10L);
        product.setName("Test");
    }

    @Test
    @DisplayName("Should create a review")
    void shouldCreateReview() {
        when(productMapper.findById(10L)).thenReturn(product);
        when(reviewMapper.countByProduct(10L)).thenReturn(2);
        when(reviewMapper.avgRatingByProduct(10L)).thenReturn(new BigDecimal("4.00"));

        Review result = reviewService.create(1L, createRequest(10L, 4, "Great!"));

        assertNotNull(result);
        verify(reviewMapper).insert(any(Review.class));
    }

    @Test
    @DisplayName("Should throw when reviewing non-existent product")
    void shouldRejectMissingProduct() {
        when(productMapper.findById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> reviewService.create(1L, createRequest(999L, 4, "Nope")));
    }

    @Test
    @DisplayName("Should get reviews by product ID")
    void shouldGetByProductId() {
        when(reviewMapper.findByProductId(10L)).thenReturn(List.of(review));

        List<Review> result = reviewService.getByProductId(10L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should delete own review")
    void shouldDeleteOwnReview() {
        when(reviewMapper.findById(1L)).thenReturn(review);
        when(reviewMapper.countByProduct(10L)).thenReturn(1);
        when(reviewMapper.avgRatingByProduct(10L)).thenReturn(new BigDecimal("3.00"));
        when(productMapper.findById(10L)).thenReturn(product);

        reviewService.delete(1L, 1L);

        verify(reviewMapper).softDelete(1L);
        verify(productMapper).update(product); // rating updated
    }

    @Test
    @DisplayName("Should throw when deleting other user's review")
    void shouldRejectDeleteOtherUsersReview() {
        when(reviewMapper.findById(1L)).thenReturn(review);

        assertThrows(BusinessException.class,
                () -> reviewService.delete(1L, 2L));
    }

    @Test
    @DisplayName("Should update AI analysis fields")
    void shouldUpdateAiFields() {
        reviewService.updateAiFields(1L, "POSITIVE", "happy",
                new BigDecimal("0.05"), null, 0);

        verify(reviewMapper).updateAiAnalysis(any(Review.class));
    }

    @Test
    @DisplayName("Should get flagged reviews")
    void shouldGetFlaggedReviews() {
        Review flagged = new Review();
        flagged.setId(2L);
        flagged.setIsFlagged(1);
        when(reviewMapper.findFlaggedReviews()).thenReturn(List.of(flagged));

        List<Review> result = reviewService.findFlaggedReviews();
        assertEquals(1, result.size());
    }

    private com.vericart.dto.ReviewRequest createRequest(Long productId, int rating, String content) {
        com.vericart.dto.ReviewRequest req = new com.vericart.dto.ReviewRequest();
        req.setProductId(productId);
        req.setRating(rating);
        req.setContent(content);
        return req;
    }
}
