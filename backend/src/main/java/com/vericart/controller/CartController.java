package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.entity.CartItem;
import com.vericart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<List<CartItem>> getCart(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(cartService.getCart(userId));
    }

    @PostMapping("/add")
    public Result<Void> add(Authentication auth, @RequestBody Map<String, Object> body) {
        Long userId = (Long) auth.getPrincipal();
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer quantity = body.containsKey("quantity")
                ? Integer.valueOf(body.get("quantity").toString()) : 1;
        cartService.addToCart(userId, productId, quantity);
        return Result.success("Added to cart", null);
    }

    @PutMapping("/{id}")
    public Result<Void> updateQuantity(Authentication auth, @PathVariable Long id,
                                        @RequestBody Map<String, Integer> body) {
        Long userId = (Long) auth.getPrincipal();
        cartService.updateQuantity(userId, id, body.get("quantity"));
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        cartService.removeFromCart(userId, id);
        return Result.success(null);
    }

    @DeleteMapping("/clear")
    public Result<Void> clear(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        cartService.clearCart(userId);
        return Result.success(null);
    }

    @GetMapping("/count")
    public Result<Integer> count(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(cartService.countByUser(userId));
    }
}
