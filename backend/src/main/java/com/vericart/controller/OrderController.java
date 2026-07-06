package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.dto.OrderRequest;
import com.vericart.entity.Order;
import com.vericart.entity.OrderItem;
import com.vericart.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<Order> create(@Valid @RequestBody OrderRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(orderService.createOrder(userId, request));
    }

    @GetMapping
    public Result<List<Order>> myOrders(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(orderService.getByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<OrderItem>> items(@PathVariable Long id) {
        return Result.success(orderService.getOrderItems(id));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        orderService.cancelOrder(id, userId);
        return Result.success("Order cancelled", null);
    }
}
