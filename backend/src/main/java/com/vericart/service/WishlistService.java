package com.vericart.service;

import com.vericart.entity.Product;
import com.vericart.entity.Wishlist;
import com.vericart.mapper.ProductMapper;
import com.vericart.mapper.WishlistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistMapper wishlistMapper;
    private final ProductMapper productMapper;

    public List<Map<String, Object>> getWishlist(Long userId) {
        List<Wishlist> items = wishlistMapper.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Wishlist item : items) {
            Product product = productMapper.findById(item.getProductId());
            if (product != null) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("id", item.getId());
                entry.put("productId", item.getProductId());
                entry.put("createdAt", item.getCreatedAt());
                entry.put("product", product);
                result.add(entry);
            }
        }
        return result;
    }

    @Transactional
    public void addItem(Long userId, Long productId) {
        if (!wishlistMapper.exists(userId, productId)) {
            Wishlist wishlist = new Wishlist();
            wishlist.setUserId(userId);
            wishlist.setProductId(productId);
            wishlistMapper.insert(wishlist);
        }
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        wishlistMapper.deleteByUserAndProduct(userId, productId);
    }

    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistMapper.exists(userId, productId);
    }
}
