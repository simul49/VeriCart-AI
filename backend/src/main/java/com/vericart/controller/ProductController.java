package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.dto.ProductRequest;
import com.vericart.entity.Category;
import com.vericart.entity.Product;
import com.vericart.mapper.CategoryMapper;
import com.vericart.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryMapper categoryMapper;

    // ---- Public endpoints ----

    @GetMapping("/products")
    public Result<List<Product>> list(@RequestParam(required = false) Long categoryId,
                                       @RequestParam(required = false) String sort,
                                       @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return Result.success(productService.search(keyword));
        }
        if (categoryId != null || sort != null) {
            return Result.success(productService.findFiltered(categoryId, sort));
        }
        return Result.success(productService.findAll());
    }

    @GetMapping("/products/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.success(categoryMapper.findAll());
    }

    /** Paginated product listing — returns {items,total,page,size,totalPages}. */
    @GetMapping("/products/page")
    public Result<Map<String, Object>> page(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 12;
        int offset = (page - 1) * size;

        List<Product> items = productService.findPaged(offset, size, categoryId, keyword, sort);
        long total = productService.countFiltered(categoryId, keyword);
        int totalPages = (int) Math.ceil((double) total / size);

        Map<String, Object> body = new HashMap<>();
        body.put("items", items);
        body.put("total", total);
        body.put("page", page);
        body.put("size", size);
        body.put("totalPages", totalPages);
        return Result.success(body);
    }

    /**
     * FR-057 — Product Comparison.
     * Returns the products side by side plus an explainable AI verdict on
     * which one is genuinely the most trustworthy.
     */
    @GetMapping("/products/compare")
    public Result<Map<String, Object>> compare(@RequestParam String ids) {
        List<Long> idList = java.util.Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .distinct()
                .limit(4)
                .toList();
        return Result.success(productService.compare(idList));
    }

    // ---- Admin / Seller endpoints ----

    @PostMapping("/seller/products")
    public Result<Product> create(@Valid @RequestBody ProductRequest request, Authentication auth) {
        Long sellerId = (Long) auth.getPrincipal();
        return Result.success(productService.create(request, sellerId));
    }

    @PutMapping("/seller/products/{id}")
    public Result<Product> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return Result.success(productService.update(id, request));
    }

    @DeleteMapping("/seller/products/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success("Deleted", null);
    }
}
