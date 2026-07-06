package com.vericart.mapper;

import com.vericart.entity.Order;
import com.vericart.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders (order_no, user_id, total_amount, status, shipping_name, shipping_phone, shipping_address, note) " +
            "VALUES (#{orderNo}, #{userId}, #{totalAmount}, #{status}, #{shippingName}, #{shippingPhone}, #{shippingAddress}, #{note})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Insert("INSERT INTO order_item (order_id, product_id, product_name, product_image, price, quantity) " +
            "VALUES (#{orderId}, #{productId}, #{productName}, #{productImage}, #{price}, #{quantity})")
    int insertItem(OrderItem item);

    @Select("SELECT * FROM orders WHERE id = #{id}")
    Order findById(Long id);

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order findByOrderNo(String orderNo);

    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Order> findByUserId(Long userId);

    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> findItemsByOrderId(Long orderId);

    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Select("<script>" +
            "SELECT * FROM orders WHERE 1=1 " +
            "<if test='status != null'>AND status = #{status}</if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<Order> findAll(@Param("status") String status);

    @Select("SELECT COUNT(*) FROM orders")
    long count();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status IN ('PAID','PROCESSING','SHIPPED','DELIVERED')")
    java.math.BigDecimal totalRevenue();
}
