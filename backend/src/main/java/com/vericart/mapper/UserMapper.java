package com.vericart.mapper;

import com.vericart.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM `user` WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM `user` WHERE email = #{email}")
    User findByEmail(String email);

    @Select("SELECT * FROM `user` WHERE username = #{username}")
    User findByUsername(String username);

    @Insert("INSERT INTO `user` (username, email, password, role, status) " +
            "VALUES (#{username}, #{email}, #{password}, #{role}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE `user` SET phone = #{phone}, avatar = #{avatar} WHERE id = #{id}")
    int updateProfile(User user);

    @Update("UPDATE `user` SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Select("SELECT * FROM `user` ORDER BY created_at DESC")
    java.util.List<User> findAll();

    @Select("SELECT COUNT(*) FROM `user` WHERE role = 'CUSTOMER'")
    long countCustomers();

    @Select("SELECT COUNT(*) FROM `user` WHERE role = 'SELLER'")
    long countSellers();
}
