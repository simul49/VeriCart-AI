package com.vericart.mapper;

import com.vericart.entity.AuditLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AuditLogMapper {

    @Insert("INSERT INTO audit_log (user_id, username, action, category, target_type, target_id, detail, ip, is_error) " +
            "VALUES (#{userId}, #{username}, #{action}, #{category}, #{targetType}, #{targetId}, #{detail}, #{ip}, #{isError})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditLog log);

    @Select("SELECT * FROM audit_log ORDER BY created_at DESC LIMIT #{limit}")
    List<AuditLog> findRecent(@Param("limit") int limit);

    @Select("SELECT * FROM audit_log WHERE category = #{category} ORDER BY created_at DESC LIMIT #{limit}")
    List<AuditLog> findByCategory(@Param("category") String category, @Param("limit") int limit);

    @Select("SELECT * FROM audit_log WHERE is_error = 1 ORDER BY created_at DESC LIMIT #{limit}")
    List<AuditLog> findErrors(@Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM audit_log")
    int countAll();

    @Select("SELECT COUNT(*) FROM audit_log WHERE is_error = 1")
    int countErrors();
}
