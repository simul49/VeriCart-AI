package com.vericart.service;

import com.vericart.common.Result;
import com.vericart.dto.OrderItemRequest;
import com.vericart.dto.OrderRequest;
import com.vericart.entity.Order;
import com.vericart.entity.Product;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.CartMapper;
import com.vericart.mapper.NotificationMapper;
import com.vericart.mapper.OrderMapper;
import com.vericart.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock private OrderMapper orderMapper;
    @Mock private ProductMapper productMapper;
    @Mock private CartMapper cartMapper;
    @Mock private NotificationMapper notificationMapper;

    @InjectMocks
    private OrderService orderService;

    private Product product;
    private OrderRequest orderRequest;
    private OrderItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Phone");
        product.setPrice(new BigDecimal("999.99"));
        product.setStock(50);

        itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(itemRequest));
        orderRequest.setShippingName("John Doe");
        orderRequest.setShippingPhone("1234567890");
        orderRequest.setShippingAddress("123 Main St");
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrder() {
        when(productMapper.findById(1L)).thenReturn(product);
        doAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return 1;
        }).when(orderMapper).insert(any(Order.class));

        Order order = orderService.createOrder(1L, orderRequest);

        assertNotNull(order);
        assertEquals(100L, order.getId());
        assertEquals("PENDING", order.getStatus());
        assertEquals(new BigDecimal("1999.98"), order.getTotalAmount());
        assertEquals(48, product.getStock()); // 50 - 2

        verify(cartMapper).clearCart(1L);
        verify(notificationMapper).insert(eq(1L), eq("Order Placed"), anyString(), eq("ORDER"));
    }

    @Test
    @DisplayName("Should throw when order has no items")
    void shouldRejectEmptyOrder() {
        orderRequest.setItems(Collections.emptyList());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, orderRequest));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("Should throw when product not found")
    void shouldRejectMissingProduct() {
        OrderItemRequest badItem = new OrderItemRequest();
        badItem.setProductId(999L);
        badItem.setQuantity(1);
        orderRequest.setItems(List.of(badItem));

        when(productMapper.findById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, orderRequest));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("Should throw when insufficient stock")
    void shouldRejectInsufficientStock() {
        itemRequest.setQuantity(100); // more than 50
        when(productMapper.findById(1L)).thenReturn(product);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder(1L, orderRequest));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("Should get order by ID")
    void shouldGetById() {
        Order mockOrder = new Order();
        mockOrder.setId(10L);
        mockOrder.setUserId(1L);
        mockOrder.setStatus("PENDING");
        when(orderMapper.findById(10L)).thenReturn(mockOrder);

        Order result = orderService.getById(10L);
        assertEquals(10L, result.getId());
    }

    @Test
    @DisplayName("Should throw when order not found")
    void shouldThrowOrderNotFound() {
        when(orderMapper.findById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> orderService.getById(999L));
    }

    @Test
    @DisplayName("Should cancel PENDING order and restore stock")
    void shouldCancelOrder() {
        Order mockOrder = new Order();
        mockOrder.setId(5L);
        mockOrder.setUserId(1L);
        mockOrder.setStatus("PENDING");
        when(orderMapper.findById(5L)).thenReturn(mockOrder);

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(3);

        com.vericart.entity.OrderItem orderItem = new com.vericart.entity.OrderItem();
        orderItem.setProductId(1L);
        orderItem.setQuantity(3);
        when(orderMapper.findItemsByOrderId(5L)).thenReturn(List.of(orderItem));

        product.setStock(10);
        when(productMapper.findById(1L)).thenReturn(product);

        orderService.cancelOrder(5L, 1L);

        verify(orderMapper).updateStatus(5L, "CANCELLED");
        assertEquals(13, product.getStock()); // 10 + 3
    }

    @Test
    @DisplayName("Should prevent cancelling non-PENDING order")
    void shouldRejectCancelNonPending() {
        Order mockOrder = new Order();
        mockOrder.setId(5L);
        mockOrder.setUserId(1L);
        mockOrder.setStatus("SHIPPED");
        when(orderMapper.findById(5L)).thenReturn(mockOrder);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(5L, 1L));
        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("Should prevent cancelling another user's order")
    void shouldRejectCancelOtherUsersOrder() {
        Order mockOrder = new Order();
        mockOrder.setId(5L);
        mockOrder.setUserId(2L); // Different user
        mockOrder.setStatus("PENDING");
        when(orderMapper.findById(5L)).thenReturn(mockOrder);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(5L, 1L));
        assertEquals(403, ex.getCode());
    }
}
