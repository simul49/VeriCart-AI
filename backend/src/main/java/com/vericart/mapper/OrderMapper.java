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

    // ---- Seller-scoped queries (Seller Dashboard) ----

    @Select("SELECT DISTINCT o.* FROM orders o " +
            "JOIN order_item oi ON o.id = oi.order_id " +
            "JOIN product p ON oi.product_id = p.id " +
            "WHERE p.seller_id = #{sellerId} " +
            "<if test='status != null and status != \"\"'>AND o.status = #{status}</if> " +
            "ORDER BY o.created_at DESC")
    List<Order> findBySeller(@Param("sellerId") Long sellerId, @Param("status") String status);

    @Select("SELECT COALESCE(SUM(oi.price * oi.quantity), 0) FROM order_item oi " +
            "JOIN product p ON oi.product_id = p.id " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE p.seller_id = #{sellerId} " +
            "AND o.status IN ('PAID','PROCESSING','SHIPPED','DELIVERED')")
    java.math.BigDecimal sellerRevenue(@Param("sellerId") Long sellerId);

    @Select("SELECT COUNT(DISTINCT o.id) FROM orders o " +
            "JOIN order_item oi ON o.id = oi.order_id " +
            "JOIN product p ON oi.product_id = p.id " +
            "WHERE p.seller_id = #{sellerId}")
    long countBySeller(@Param("sellerId") Long sellerId);

    /** Order items belonging to a specific seller within one order. */
    @Select("SELECT oi.* FROM order_item oi " +
            "JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = #{orderId} AND p.seller_id = #{sellerId}")
    List<OrderItem> findSellerItems(@Param("orderId") Long orderId, @Param("sellerId") Long sellerId);
}
