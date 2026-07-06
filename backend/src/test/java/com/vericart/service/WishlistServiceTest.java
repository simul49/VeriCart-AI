package com.vericart.service;

import com.vericart.entity.Product;
import com.vericart.entity.Wishlist;
import com.vericart.mapper.ProductMapper;
import com.vericart.mapper.WishlistMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishlistService Unit Tests")
class WishlistServiceTest {

    @Mock private WishlistMapper wishlistMapper;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private WishlistService wishlistService;

    @BeforeEach
    void setUp() {
        // mocks are auto-initialized by MockitoExtension
    }

    @Test
    @DisplayName("Should add item to wishlist")
    void shouldAddItem() {
        when(wishlistMapper.exists(1L, 10L)).thenReturn(false);

        wishlistService.addItem(1L, 10L);

        verify(wishlistMapper).insert(any(Wishlist.class));
    }

    @Test
    @DisplayName("Should not duplicate item if already in wishlist")
    void shouldNotDuplicate() {
        when(wishlistMapper.exists(1L, 10L)).thenReturn(true);

        wishlistService.addItem(1L, 10L);

        verify(wishlistMapper, never()).insert(any());
    }

    @Test
    @DisplayName("Should remove item from wishlist")
    void shouldRemoveItem() {
        wishlistService.removeItem(1L, 10L);

        verify(wishlistMapper).deleteByUserAndProduct(1L, 10L);
    }

    @Test
    @DisplayName("Should check if item is in wishlist")
    void shouldCheckWishlist() {
        when(wishlistMapper.exists(1L, 10L)).thenReturn(true);

        assertTrue(wishlistService.isInWishlist(1L, 10L));
        assertFalse(wishlistService.isInWishlist(1L, 99L));
    }

    @Test
    @DisplayName("Should get wishlist with product details")
    void shouldGetWishlist() {
        Wishlist item = new Wishlist();
        item.setId(1L);
        item.setUserId(1L);
        item.setProductId(10L);

        Product product = new Product();
        product.setId(10L);
        product.setName("Cool Gadget");
        product.setPrice(new BigDecimal("29.99"));

        when(wishlistMapper.findByUserId(1L)).thenReturn(List.of(item));
        when(productMapper.findById(10L)).thenReturn(product);

        List<Map<String, Object>> result = wishlistService.getWishlist(1L);

        assertEquals(1, result.size());
        assertEquals("Cool Gadget", ((Product) result.get(0).get("product")).getName());
    }

    @Test
    @DisplayName("Should return empty list for user with no wishlist")
    void shouldReturnEmptyWishlist() {
        when(wishlistMapper.findByUserId(1L)).thenReturn(List.of());

        List<Map<String, Object>> result = wishlistService.getWishlist(1L);
        assertTrue(result.isEmpty());
    }
}
