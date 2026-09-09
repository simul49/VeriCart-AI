package com.vericart.dto;

import com.vericart.entity.Product;
import lombok.Data;

import java.util.List;

/**
 * One sub-category inside a main-category section on the "All Products" browse view:
 * holds the sub-category's metadata and the product cards that belong to it.
 */
@Data
public class SubcategorySectionDTO {
    private Long id;
    private String name;
    private String images;          // JSON array of offline/local image URLs
    private List<Product> products; // product cards to render for this sub-category
}
