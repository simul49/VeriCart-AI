package com.vericart.service;

import com.vericart.dto.OrderItemRequest;
import com.vericart.dto.OrderRequest;
import com.vericart.entity.*;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final CartMapper cartMapper;
    private final NotificationMapper notificationMapper;

    @Transactional
    public Order createOrder(Long userId, OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException(400, "Order must contain at least one item");
        }

        // Generate order number
        String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        BigDecimal total = BigDecimal.ZERO;

        // Validate items and calculate total
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productMapper.findById(itemReq.getProductId());
            if (product == null) throw new BusinessException(404, "Product not found: " + itemReq.getProductId());
            if (product.getStock() < itemReq.getQuantity()) {
                throw new BusinessException(400, "Insufficient stock for: " + product.getName());
            }
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        // Create order
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus("PENDING");
        order.setShippingName(request.getShippingName());
        order.setShippingPhone(request.getShippingPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setNote(request.getNote());
        orderMapper.insert(order);

        // Create order items & update stock
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productMapper.findById(itemReq.getProductId());
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(extractFirstImage(product.getImages()));
            item.setPrice(product.getPrice());
            item.setQuantity(itemReq.getQuantity());
            orderMapper.insertItem(item);

            // Update stock
            product.setStock(product.getStock() - itemReq.getQuantity());
            productMapper.update(product);
        }

        // Clear cart for those items
        cartMapper.clearCart(userId);

        // Notification
        notificationMapper.insert(userId, "Order Placed",
                "Your order #" + orderNo + " has been placed successfully.", "ORDER");

        return order;
    }

    public Order getById(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null) throw new BusinessException(404, "Order not found");
        return order;
    }

    public List<Order> getByUserId(Long userId) {
        return orderMapper.findByUserId(userId);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderMapper.findItemsByOrderId(orderId);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) throw new BusinessException(404, "Order not found");
        if (!order.getUserId().equals(userId)) throw new BusinessException(403, "Access denied");
        if (!"PENDING".equals(order.getStatus())) throw new BusinessException(400, "Only pending orders can be cancelled");

        orderMapper.updateStatus(orderId, "CANCELLED");

        // Restore stock
        List<OrderItem> items = orderMapper.findItemsByOrderId(orderId);
        for (OrderItem item : items) {
            Product product = productMapper.findById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productMapper.update(product);
            }
        }
    }

    public List<Order> findAll(String status) {
        return orderMapper.findAll(status);
    }

    @Transactional
    public void updateStatus(Long orderId, String status) {
        orderMapper.updateStatus(orderId, status);
    }

    public long count() {
        return orderMapper.count();
    }

    public BigDecimal totalRevenue() {
        return orderMapper.totalRevenue();
    }

    private String extractFirstImage(String images) {
        if (images == null) return null;
        try {
            if (images.startsWith("[")) {
                String cleaned = images.replaceAll("[\\[\\]\"]", "");
                String[] parts = cleaned.split(",");
                return parts.length > 0 ? parts[0].trim() : null;
            }
        } catch (Exception ignored) {}
        return images;
    }
}
