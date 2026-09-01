package com.vericart.service;

import com.vericart.dto.ProductRequest;
import com.vericart.entity.Product;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

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
