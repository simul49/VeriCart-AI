package com.vericart.mapper;

import com.vericart.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    // ---- Original API (used by OrderService) ----

    @Insert("INSERT INTO notification (user_id, title, content, type) VALUES (#{userId}, #{title}, #{content}, #{type})")
    int insert(@Param("userId") Long userId, @Param("title") String title,
               @Param("content") String content, @Param("type") String type);

    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(Long userId);

    // ---- Extended API (Notification module UI) ----

    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<Notification> findByUser(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM notification WHERE id = #{id}")
    Notification findById(Long id);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId}")
    int markAllRead(Long userId);

    @Delete("DELETE FROM notification WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);
}
