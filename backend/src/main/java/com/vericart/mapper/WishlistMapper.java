package com.vericart.mapper;

import com.vericart.entity.Wishlist;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WishlistMapper {

    @Select("SELECT * FROM wishlist WHERE user_id = #{userId}")
    List<Wishlist> findByUserId(Long userId);

    @Insert("INSERT INTO wishlist (user_id, product_id, created_at) VALUES (#{userId}, #{productId}, CURRENT_TIMESTAMP)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Wishlist wishlist);

    @Delete("DELETE FROM wishlist WHERE user_id = #{userId} AND product_id = #{productId}")
    int deleteByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT COUNT(*) > 0 FROM wishlist WHERE user_id = #{userId} AND product_id = #{productId}")
    boolean exists(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT p.* FROM wishlist w JOIN product p ON w.product_id = p.id WHERE w.user_id = #{userId}")
    List<Wishlist> findWishlistProductsByUser(Long userId);
}
