package com.vericart.service;

import com.vericart.entity.CartItem;
import com.vericart.entity.Product;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.CartMapper;
import com.vericart.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock private CartMapper cartMapper;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private CartService cartService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Item");
        product.setPrice(new BigDecimal("49.99"));
        product.setStock(100);
    }

    @Test
    @DisplayName("Should add item to cart when product exists")
    void shouldAddToCart() {
        when(productMapper.findById(1L)).thenReturn(product);
        when(cartMapper.findByUserAndProduct(1L, 1L)).thenReturn(null);

        cartService.addToCart(1L, 1L, 2);

        verify(cartMapper).insert(any(CartItem.class));
    }

    @Test
    @DisplayName("Should update quantity when item already in cart")
    void shouldUpdateExistingCartItem() {
        CartItem existing = new CartItem();
        existing.setId(10L);
        existing.setQuantity(1);
        when(productMapper.findById(1L)).thenReturn(product);
        when(cartMapper.findByUserAndProduct(1L, 1L)).thenReturn(existing);

        cartService.addToCart(1L, 1L, 3);

        verify(cartMapper).updateQuantity(10L, 4); // 1 + 3
    }

    @Test
    @DisplayName("Should throw when product not found")
    void shouldRejectMissingProduct() {
        when(productMapper.findById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> cartService.addToCart(1L, 999L, 1));
    }

    @Test
    @DisplayName("Should throw when stock insufficient")
    void shouldRejectInsufficientStock() {
        product.setStock(1);
        when(productMapper.findById(1L)).thenReturn(product);

        assertThrows(BusinessException.class,
                () -> cartService.addToCart(1L, 1L, 10));
    }

    @Test
    @DisplayName("Should get user cart")
    void shouldGetCart() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProductId(1L);
        item.setQuantity(2);
        when(cartMapper.findByUserId(1L)).thenReturn(List.of(item));

        List<CartItem> result = cartService.getCart(1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should update quantity")
    void shouldUpdateQuantity() {
        cartService.updateQuantity(1L, 5L, 3);

        verify(cartMapper).updateQuantity(5L, 3);
    }

    @Test
    @DisplayName("Should remove from cart")
    void shouldRemoveFromCart() {
        cartService.removeFromCart(1L, 5L);

        verify(cartMapper).delete(5L);
    }

    @Test
    @DisplayName("Should clear cart")
    void shouldClearCart() {
        cartService.clearCart(1L);

        verify(cartMapper).clearCart(1L);
    }

    @Test
    @DisplayName("Should count cart items")
    void shouldCountCart() {
        when(cartMapper.countByUser(1L)).thenReturn(3);

        assertEquals(3, cartService.countByUser(1L));
    }
}
