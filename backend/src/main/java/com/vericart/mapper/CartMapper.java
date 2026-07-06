package com.vericart.mapper;

import com.vericart.entity.CartItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartMapper {

    @Select("SELECT ci.*, p.name as product_name, p.price as product_price, " +
            "p.images as product_image " +
            "FROM cart_item ci JOIN product p ON ci.product_id = p.id " +
            "WHERE ci.user_id = #{userId} ORDER BY ci.created_at DESC")
    List<CartItem> findByUserId(Long userId);

    @Select("SELECT * FROM cart_item WHERE user_id = #{userId} AND product_id = #{productId}")
    CartItem findByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Insert("INSERT INTO cart_item (user_id, product_id, quantity) VALUES (#{userId}, #{productId}, #{quantity})")
    int insert(CartItem item);

    @Update("UPDATE cart_item SET quantity = #{quantity} WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Delete("DELETE FROM cart_item WHERE id = #{id}")
    int delete(Long id);

    @Delete("DELETE FROM cart_item WHERE user_id = #{userId}")
    int clearCart(Long userId);

    @Select("SELECT COUNT(*) FROM cart_item WHERE user_id = #{userId}")
    int countByUser(Long userId);
}
