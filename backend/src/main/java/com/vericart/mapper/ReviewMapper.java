package com.vericart.mapper;

import com.vericart.entity.Review;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReviewMapper {

    @Select("SELECT r.*, u.username, u.avatar as user_avatar FROM review r " +
            "JOIN `user` u ON r.user_id = u.id WHERE r.product_id = #{productId} AND r.status = 1 " +
            "ORDER BY r.created_at DESC")
    List<Review> findByProductId(Long productId);

    @Select("SELECT * FROM review WHERE id = #{id}")
    Review findById(Long id);

    @Insert("INSERT INTO review (user_id, product_id, order_id, rating, content, images) " +
            "VALUES (#{userId}, #{productId}, #{orderId}, #{rating}, #{content}, #{images})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Review review);

    @Update("UPDATE review SET rating = #{rating}, content = #{content}, images = #{images} WHERE id = #{id}")
    int update(Review review);

    @Update("UPDATE review SET sentiment = #{sentiment}, emotion = #{emotion}, " +
            "fake_probability = #{fakeProbability}, fake_reason = #{fakeReason}, is_flagged = #{isFlagged} " +
            "WHERE id = #{id}")
    int updateAiAnalysis(Review review);

    @Update("UPDATE review SET status = 0 WHERE id = #{id}")
    int softDelete(Long id);

    @Select("SELECT r.*, u.username FROM review r JOIN `user` u ON r.user_id = u.id " +
            "WHERE r.is_flagged = 1 ORDER BY r.created_at DESC")
    List<Review> findFlaggedReviews();

    @Select("SELECT COUNT(*) FROM review WHERE product_id = #{productId} AND status = 1")
    int countByProduct(Long productId);

    @Select("SELECT AVG(rating) FROM review WHERE product_id = #{productId} AND status = 1")
    java.math.BigDecimal avgRatingByProduct(Long productId);

    @Select("SELECT * FROM review WHERE is_flagged = 0 AND sentiment IS NULL AND status = 1 LIMIT #{limit}")
    List<Review> findReviewsNeedingAnalysis(@Param("limit") int limit);
}
