package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public Result<?> list(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(wishlistService.getWishlist(userId));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Map<String, Long> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Long productId = body.get("productId");
        wishlistService.addItem(userId, productId);
        return Result.success(Map.of("message", "Added to wishlist"));
    }

    @DeleteMapping("/{productId}")
    public Result<?> remove(@PathVariable Long productId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        wishlistService.removeItem(userId, productId);
        return Result.success(Map.of("message", "Removed from wishlist"));
    }

    @GetMapping("/check/{productId}")
    public Result<?> check(@PathVariable Long productId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        boolean exists = wishlistService.isInWishlist(userId, productId);
        return Result.success(Map.of("inWishlist", exists));
    }
}
