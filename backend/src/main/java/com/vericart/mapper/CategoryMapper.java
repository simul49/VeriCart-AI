package com.vericart.mapper;

import com.vericart.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category WHERE status = 1 ORDER BY sort_order")
    List<Category> findAll();

    @Select("SELECT * FROM category WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort_order")
    List<Category> findByParentId(Long parentId);

    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(Long id);

    @Insert("INSERT INTO category (name, parent_id, sort_order) VALUES (#{name}, #{parentId}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE category SET name=#{name}, parent_id=#{parentId}, sort_order=#{sortOrder} WHERE id=#{id}")
    int update(Category category);

    @Update("UPDATE category SET status = 0 WHERE id = #{id}")
    int delete(Long id);
}
