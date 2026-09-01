package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.entity.Order;
import com.vericart.entity.OrderItem;
import com.vericart.entity.Product;
import com.vericart.entity.Review;
import com.vericart.mapper.OrderMapper;
import com.vericart.mapper.ReviewMapper;
import com.vericart.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seller Dashboard API — product listings, order fulfilment and AI review monitoring
 * for the authenticated seller (PRD FR-033/064 seller value proposition).
 */
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final ProductService productService;
    private final OrderMapper orderMapper;
    private final ReviewMapper reviewMapper;

    /** Products owned by the current seller. */
    @GetMapping("/products")
    public Result<List<Product>> myProducts(Authentication auth) {
        Long sellerId = (Long) auth.getPrincipal();
        return Result.success(productService.findBySeller(sellerId));
    }

    /** Orders containing the seller's products. */
    @GetMapping("/orders")
    public Result<List<Order>> myOrders(@RequestParam(required = false) String status,
                                        Authentication auth) {
        Long sellerId = (Long) auth.getPrincipal();
        return Result.success(orderMapper.findBySeller(sellerId, status));
    }

    /** Line items of one order that belong to this seller. */
    @GetMapping("/orders/{id}/items")
    public Result<List<OrderItem>> orderItems(@PathVariable Long id, Authentication auth) {
        Long sellerId = (Long) auth.getPrincipal();
        return Result.success(orderMapper.findSellerItems(id, sellerId));
    }

    /** Advance an order status (e.g. PENDING → SHIPPED). */
    @PutMapping("/orders/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          Authentication auth) {
        Long sellerId = (Long) auth.getPrincipal();
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return Result.error(400, "status is required");
        }
        // Ensure the order actually contains this seller's products
        List<OrderItem> mine = orderMapper.findSellerItems(id, sellerId);
        if (mine.isEmpty()) {
            return Result.error(403, "This order does not contain your products");
        }
        orderMapper.updateStatus(id, status);
        return Result.success("Order updated", null);
    }

    /** All reviews on the seller's products, including AI analysis results. */
    @GetMapping("/reviews")
    public Result<List<Review>> myReviews(Authentication auth) {
        Long sellerId = (Long) auth.getPrincipal();
        return Result.success(reviewMapper.findBySeller(sellerId));
    }

    /** Aggregated dashboard statistics for the seller. */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(Authentication auth) {
        Long sellerId = (Long) auth.getPrincipal();
        List<Product> products = productService.findBySeller(sellerId);
        List<Review> reviews = reviewMapper.findBySeller(sellerId);

        BigDecimal revenue = orderMapper.sellerRevenue(sellerId);
        long orderCount = orderMapper.countBySeller(sellerId);

        long flagged = reviews.stream().filter(r -> Integer.valueOf(1).equals(r.getIsFlagged())).count();
        long positive = reviews.stream().filter(r -> "POSITIVE".equalsIgnoreCase(r.getSentiment())).count();
        double avgRating = reviews.stream().filter(r -> r.getRating() != null)
                .mapToInt(Review::getRating).average().orElse(0.0);
        int lowStock = (int) products.stream().filter(p -> p.getStock() != null && p.getStock() < 10).count();

        Map<String, Object> s = new HashMap<>();
        s.put("productCount", products.size());
        s.put("orderCount", orderCount);
        s.put("revenue", revenue != null ? revenue : BigDecimal.ZERO);
        s.put("reviewCount", reviews.size());
        s.put("flaggedReviews", flagged);
        s.put("positiveReviews", positive);
        s.put("avgRating", Math.round(avgRating * 10.0) / 10.0);
        s.put("lowStockCount", lowStock);
        return Result.success(s);
    }
}
