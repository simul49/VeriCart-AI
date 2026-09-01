package com.vericart.mapper;

import com.vericart.entity.Inquiry;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface InquiryMapper {

    @Insert("""
        INSERT INTO inquiry (product_id, user_id, seller_id, message)
        VALUES (#{productId}, #{userId}, #{sellerId}, #{message})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Inquiry inquiry);

    /**
     * Columns are aliased to camelCase explicitly: map-underscore-to-camel-case only
     * applies to POJO result maps, NOT to plain Map results — without the aliases the
     * frontend receives snake_case keys (product_name, created_at…) and renders blanks.
     */
    @Select("""
        SELECT i.id, i.product_id AS productId, i.user_id AS userId, i.seller_id AS sellerId,
               i.message, i.reply, i.reply_source AS replySource, i.status,
               i.is_read AS isRead, i.created_at AS createdAt, i.updated_at AS updatedAt,
               p.name AS productName, u.username AS userName
        FROM inquiry i
        JOIN product p ON p.id = i.product_id
        JOIN user u ON u.id = i.user_id
        WHERE i.user_id = #{userId}
        ORDER BY i.created_at DESC
        """)
    List<Map<String, Object>> findByUser(Long userId);

    @Select("""
        SELECT i.id, i.product_id AS productId, i.user_id AS userId, i.seller_id AS sellerId,
               i.message, i.reply, i.reply_source AS replySource, i.status,
               i.is_read AS isRead, i.created_at AS createdAt, i.updated_at AS updatedAt,
               p.name AS productName, p.price AS productPrice,
               u.username AS userName, u.email AS userEmail
        FROM inquiry i
        JOIN product p ON p.id = i.product_id
        JOIN user u ON u.id = i.user_id
        WHERE i.seller_id = #{sellerId}
        ORDER BY i.is_read ASC, i.created_at DESC
        """)
    List<Map<String, Object>> findBySeller(Long sellerId);

    @Select("SELECT * FROM inquiry WHERE id = #{id}")
    Inquiry findById(Long id);

    /** Customer sent a new turn → thread is open again and the seller should see it as NEW. */
    @Update("""
        UPDATE inquiry
        SET status = 'OPEN', is_read = 0, updated_at = NOW()
        WHERE id = #{id}
        """)
    int markNewMessage(Long id);

    /** A reply (AI or seller) landed → thread is answered. is_read stays untouched so NEW badges survive. */
    @Update("""
        UPDATE inquiry
        SET status = 'REPLIED', updated_at = NOW()
        WHERE id = #{id}
        """)
    int markReplied(Long id);

    @Update("UPDATE inquiry SET is_read = 1, updated_at = NOW() WHERE id = #{id} AND seller_id = #{sellerId}")
    int markRead(@Param("id") Long id, @Param("sellerId") Long sellerId);

    @Select("SELECT COUNT(*) FROM inquiry WHERE seller_id = #{sellerId} AND is_read = 0")
    int countUnreadBySeller(Long sellerId);
}
