package com.vericart.mapper;

import com.vericart.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("SELECT * FROM product WHERE id = #{id} AND status = 1")
    Product findById(Long id);

    @Select("SELECT * FROM product WHERE status = 1 ORDER BY created_at DESC")
    List<Product> findAllSimple();

    @Select("SELECT * FROM product WHERE status = 1 AND category_id = #{categoryId} ORDER BY created_at DESC")
    List<Product> findByCategory(Long categoryId);

    @Select("SELECT * FROM product WHERE status = 1 AND (name LIKE CONCAT('%',#{keyword},'%') OR description LIKE CONCAT('%',#{keyword},'%')) ORDER BY trust_score DESC")
    List<Product> search(String keyword);

    @Select("<script>" +
            "SELECT * FROM product WHERE status = 1 " +
            "<if test='categoryId != null'>AND category_id = #{categoryId}</if>" +
            "<if test='keyword != null and keyword != \"\"'>AND (name LIKE CONCAT('%',#{keyword},'%') OR description LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "ORDER BY " +
            "<choose>" +
            "<when test='sortBy == \"price_asc\"'>price ASC</when>" +
            "<when test='sortBy == \"price_desc\"'>price DESC</when>" +
            "<when test='sortBy == \"trust\"'>trust_score DESC</when>" +
            "<otherwise>created_at DESC</otherwise>" +
            "</choose>" +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Product> findAll(@Param("offset") int offset, @Param("limit") int limit,
                          @Param("categoryId") Long categoryId, @Param("keyword") String keyword,
                          @Param("sortBy") String sortBy);

    @Select("<script>" +
            "SELECT * FROM product WHERE status = 1 " +
            "<if test='categoryId != null'>AND category_id = #{categoryId}</if>" +
            "ORDER BY " +
            "<choose>" +
            "<when test='sortBy == \"price_asc\"'>price ASC</when>" +
            "<when test='sortBy == \"price_desc\"'>price DESC</when>" +
            "<when test='sortBy == \"trust\"'>trust_score DESC</when>" +
            "<otherwise>created_at DESC</otherwise>" +
            "</choose>" +
            "</script>")
    List<Product> findFiltered(@Param("categoryId") Long categoryId, @Param("sortBy") String sortBy);

    @Insert("INSERT INTO product (name, description, price, stock, category_id, brand, images, specifications, seller_id, status) " +
            "VALUES (#{name}, #{description}, #{price}, #{stock}, #{categoryId}, #{brand}, #{images}, #{specifications}, #{sellerId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE product SET name=#{name}, description=#{description}, price=#{price}, stock=#{stock}, " +
            "category_id=#{categoryId}, brand=#{brand}, images=#{images}, specifications=#{specifications} WHERE id=#{id}")
    int update(Product product);

    @Update("UPDATE product SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE product SET rating = #{rating}, review_count = #{reviewCount}, " +
            "trust_score = #{trustScore}, trust_level = #{trustLevel}, ai_summary = #{aiSummary}, " +
            "ai_summary_time = CURRENT_TIMESTAMP, fake_review_count = #{fakeReviewCount} WHERE id = #{id}")
    int updateAiFields(Product product);

    @Delete("DELETE FROM product WHERE id = #{id}")
    int delete(Long id);

    @Select("SELECT COUNT(*) FROM product WHERE status = 1")
    long count();

    @Select("SELECT * FROM product WHERE (trust_score IS NULL OR ai_summary IS NULL) AND review_count > 0 AND status = 1 LIMIT #{limit}")
    List<Product> findProductsNeedingAiUpdate(@Param("limit") int limit);
}
