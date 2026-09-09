package com.vericart.service;

import com.vericart.dto.CategorySectionDTO;
import com.vericart.dto.ProductRequest;
import com.vericart.dto.SubcategorySectionDTO;
import com.vericart.entity.Category;
import com.vericart.entity.Product;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.CategoryMapper;
import com.vericart.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    public Product getById(Long id) {
        Product product = productMapper.findById(id);
        if (product == null) throw new BusinessException(404, "Product not found");
        return product;
    }

    public List<Product> findAll() {
        return productMapper.findAllSimple();
    }

    public List<Product> findByCategory(Long categoryId) {
        return productMapper.findByCategory(categoryId);
    }

    public List<Product> search(String keyword) {
        return productMapper.search(keyword);
    }

    public List<Product> findFiltered(Long categoryId, String sortBy) {
        return productMapper.findFiltered(categoryId, sortBy);
    }

    @Transactional
    public Product create(ProductRequest request, Long sellerId) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategoryId(request.getCategoryId());
        product.setBrand(request.getBrand());
        product.setImages(request.getImages());
        product.setSpecifications(request.getSpecifications());
        product.setExternalUrl(request.getExternalUrl());
        product.setVariants(request.getVariants());
        product.setSellerId(sellerId);
        product.setStatus(1);
        productMapper.insert(product);
        return product;
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product existing = productMapper.findById(id);
        if (existing == null) throw new BusinessException(404, "Product not found");

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStock(request.getStock());
        existing.setCategoryId(request.getCategoryId());
        existing.setBrand(request.getBrand());
        existing.setImages(request.getImages());
        existing.setSpecifications(request.getSpecifications());
        existing.setExternalUrl(request.getExternalUrl());
        existing.setVariants(request.getVariants());

        productMapper.update(existing);
        return existing;
    }

    @Transactional
    public void delete(Long id) {
        productMapper.updateStatus(id, 0);
    }

    public long count() {
        return productMapper.count();
    }

    public List<Product> findNeedingAiUpdate(int limit) {
        return productMapper.findProductsNeedingAiUpdate(limit);
    }

    // ---- Pagination ----

    public List<Product> findPaged(int offset, int limit, Long categoryId, String keyword, String sortBy) {
        return productMapper.findAll(offset, limit, categoryId, keyword, sortBy);
    }

    public long countFiltered(Long categoryId, String keyword) {
        return productMapper.countFiltered(categoryId, keyword);
    }

    public List<Product> findBySeller(Long sellerId) {
        return productMapper.findBySeller(sellerId);
    }

    /**
     * FR — Browse "All Products" grouped by main category → sub-category, with the
     * product cards for each sub-category. Only sub-categories that actually contain
     * products are returned, so the page never shows empty sections.
     *
     * Products assigned directly to a MAIN category (i.e. not to any sub-category)
     * are intentionally excluded here — they are the uncategorised "other" products
     * and are not part of the per-sub-category browsing experience.
     *
     * @param mainCategoryId optional — restrict to a single main category
     */
    public List<CategorySectionDTO> findGroupedBySubcategory(Long mainCategoryId) {
        List<Product> allProducts = productMapper.findAllSimple();   // status = 1 only
        List<Category> allCategories = categoryMapper.findAll();      // status = 1 only

        Map<Long, Category> catById = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));

        // Attach each product to its sub-category (parent_id != null). Products on a
        // main category (or with an unknown category) are skipped.
        Map<Long, List<Product>> productsBySub = new HashMap<>();
        for (Product p : allProducts) {
            Long cid = p.getCategoryId();
            if (cid == null) continue;
            Category c = catById.get(cid);
            if (c == null || c.getParentId() == null) continue; // main/unknown → not shown
            productsBySub.computeIfAbsent(c.getId(), k -> new ArrayList<>()).add(p);
        }

        List<Category> mains = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .filter(c -> mainCategoryId == null || c.getId().equals(mainCategoryId))
                .sorted(Comparator.comparingInt(c -> c.getSortOrder() == null ? 0 : c.getSortOrder()))
                .toList();

        List<CategorySectionDTO> result = new ArrayList<>();
        for (Category main : mains) {
            List<SubcategorySectionDTO> subs = allCategories.stream()
                    .filter(c -> main.getId().equals(c.getParentId()))
                    .sorted(Comparator.comparingInt(c -> c.getSortOrder() == null ? 0 : c.getSortOrder()))
                    .map(sub -> {
                        SubcategorySectionDTO dto = new SubcategorySectionDTO();
                        dto.setId(sub.getId());
                        dto.setName(sub.getName());
                        dto.setImages(sub.getImages());
                        dto.setProducts(productsBySub.getOrDefault(sub.getId(), List.of()));
                        return dto;
                    })
                    .filter(dto -> dto.getProducts() != null && !dto.getProducts().isEmpty())
                    .collect(Collectors.toList());

            if (subs.isEmpty()) continue; // skip main categories with no sub-category products

            CategorySectionDTO section = new CategorySectionDTO();
            section.setId(main.getId());
            section.setName(main.getName());
            section.setImages(main.getImages());
            section.setSubcategories(subs);
            result.add(section);
        }
        return result;
    }

    /**
     * FR-057 — Product Comparison with an explainable AI verdict.
     * Picks the most trustworthy option and explains the reasoning,
     * honouring PRD Principle 2 (Explainable AI).
     */
    public Map<String, Object> compare(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "Provide at least one product id to compare");
        }
        List<Product> products = productMapper.findByIds(ids);
        if (products.isEmpty()) throw new BusinessException(404, "No products found for the given ids");

        Product best = null;
        double bestScore = -1;
        for (Product p : products) {
            double score = 0;
            score += (p.getTrustScore() != null ? p.getTrustScore() : 50) * 0.5;         // trust dominates
            score += (p.getRating() != null ? p.getRating().doubleValue() : 0) * 10;    // rating 0-5 → 0-50
            score += Math.max(0, 10 - (p.getReviewCount() == null ? 0 : Math.min(10, p.getReviewCount() / 20)));
            if (score > bestScore) {
                bestScore = score;
                best = p;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("winnerId", best != null ? best.getId() : null);
        result.put("winnerName", best != null ? best.getName() : null);
        result.put("verdict", buildCompareVerdict(products, best));
        return result;
    }

    private String buildCompareVerdict(List<Product> products, Product best) {
        if (best == null) return "Not enough data to compare.";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Based on AI analysis, **%s** is the most trustworthy choice.%n%n", best.getName()));

        int trust = best.getTrustScore() != null ? best.getTrustScore() : 0;
        sb.append(String.format("- **Trust Score:** %d/100 (%s)%n",
                trust, best.getTrustLevel() != null ? best.getTrustLevel() : "Not rated"));
        sb.append(String.format("- **Rating:** %.1f/5 from %d reviews%n",
                best.getRating() != null ? best.getRating().doubleValue() : 0.0,
                best.getReviewCount() != null ? best.getReviewCount() : 0));
        if (best.getFakeReviewCount() != null && best.getFakeReviewCount() > 0) {
            sb.append(String.format("- ⚠️ %d suspicious review(s) detected and excluded%n", best.getFakeReviewCount()));
        }

        Product cheapest = products.stream()
                .filter(p -> p.getPrice() != null)
                .min(java.util.Comparator.comparing(Product::getPrice)).orElse(null);
        if (cheapest != null && !cheapest.getId().equals(best.getId())) {
            sb.append(String.format("- 💰 Cheapest alternative: **%s** at $%.2f%n",
                    cheapest.getName(), cheapest.getPrice().doubleValue()));
        }
        return sb.toString();
    }
}
