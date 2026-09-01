package com.vericart.mapper;

import com.vericart.entity.InquiryMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InquiryMessageMapper {

    @Insert("""
        INSERT INTO inquiry_message (inquiry_id, sender, content, is_read)
        VALUES (#{inquiryId}, #{sender}, #{content}, 0)
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(InquiryMessage message);

    @Select("""
        SELECT * FROM inquiry_message
        WHERE inquiry_id = #{inquiryId}
        ORDER BY id ASC
        """)
    List<InquiryMessage> findByInquiry(Long inquiryId);

    /** Mark all turns from a given sender as read. */
    @Update("""
        UPDATE inquiry_message SET is_read = 1
        WHERE inquiry_id = #{inquiryId} AND sender = #{sender}
        """)
    int markRead(@Param("inquiryId") Long inquiryId, @Param("sender") String sender);

    /** The sender of the last turn in a thread. */
    @Select("""
        SELECT sender FROM inquiry_message
        WHERE inquiry_id = #{inquiryId}
        ORDER BY id DESC LIMIT 1
        """)
    String lastSender(Long inquiryId);
}
