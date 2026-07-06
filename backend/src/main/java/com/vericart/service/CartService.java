package com.vericart.service;

import com.vericart.entity.CartItem;
import com.vericart.entity.Product;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.CartMapper;
import com.vericart.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public List<CartItem> getCart(Long userId) {
        List<CartItem> items = cartMapper.findByUserId(userId);
        // Parse first image from JSON array for H2 compatibility
        for (CartItem item : items) {
            item.setProductImage(extractFirstImage(item.getProductImage()));
        }
        return items;
    }

    @Transactional
    public void addToCart(Long userId, Long productId, Integer quantity) {
        Product product = productMapper.findById(productId);
        if (product == null) throw new BusinessException(404, "Product not found");
        if (product.getStock() < quantity) throw new BusinessException(400, "Insufficient stock");

        CartItem existing = cartMapper.findByUserAndProduct(userId, productId);
        if (existing != null) {
            cartMapper.updateQuantity(existing.getId(), existing.getQuantity() + quantity);
        } else {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            cartMapper.insert(item);
        }
    }

    @Transactional
    public void updateQuantity(Long userId, Long itemId, Integer quantity) {
        cartMapper.updateQuantity(itemId, quantity);
    }

    @Transactional
    public void removeFromCart(Long userId, Long itemId) {
        cartMapper.delete(itemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartMapper.clearCart(userId);
    }

    public int countByUser(Long userId) {
        return cartMapper.countByUser(userId);
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
