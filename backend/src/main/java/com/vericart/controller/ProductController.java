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

import java.util.List;

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
