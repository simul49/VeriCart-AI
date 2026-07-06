package com.vericart.service;

import com.vericart.entity.Product;
import com.vericart.exception.BusinessException;
import com.vericart.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Phone");
        product.setDescription("A great phone");
        product.setPrice(new BigDecimal("999.99"));
        product.setStock(50);
        product.setCategoryId(6L);
        product.setBrand("TestBrand");
        product.setStatus(1);
    }

    @Test
    @DisplayName("Should return product by ID")
    void shouldGetById() {
        when(productMapper.findById(1L)).thenReturn(product);

        Product result = productService.getById(1L);
        assertEquals("Test Phone", result.getName());
    }

    @Test
    @DisplayName("Should throw when product not found")
    void shouldThrowWhenNotFound() {
        when(productMapper.findById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> productService.getById(999L));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("Should return all products")
    void shouldFindAll() {
        when(productMapper.findAllSimple()).thenReturn(List.of(product));

        List<Product> result = productService.findAll();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should search by keyword")
    void shouldSearch() {
        when(productMapper.search("phone")).thenReturn(List.of(product));

        List<Product> result = productService.search("phone");
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should soft-delete product by setting status to 0")
    void shouldSoftDelete() {
        productService.delete(1L);

        verify(productMapper).updateStatus(1L, 0);
    }

    @Test
    @DisplayName("Should return product count")
    void shouldCount() {
        when(productMapper.count()).thenReturn(10L);

        assertEquals(10L, productService.count());
    }
}
