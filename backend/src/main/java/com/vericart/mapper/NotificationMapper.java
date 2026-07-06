package com.vericart.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface NotificationMapper {

    @Insert("INSERT INTO notification (user_id, title, content, type) VALUES (#{userId}, #{title}, #{content}, #{type})")
    int insert(@Param("userId") Long userId, @Param("title") String title,
               @Param("content") String content, @Param("type") String type);

    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(Long userId);
}
