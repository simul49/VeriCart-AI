package com.vericart.service;

import com.vericart.dto.ProductRequest;
import com.vericart.entity.Product;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
