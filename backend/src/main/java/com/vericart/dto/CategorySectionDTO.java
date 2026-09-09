package com.vericart.dto;

import lombok.Data;

import java.util.List;

/**
 * A main category on the "All Products" browse view, containing its sub-categories.
 * Only main categories that have at least one sub-category with products are returned,
 * so the UI never renders empty sections.
 */
@Data
public class CategorySectionDTO {
    private Long id;
    private String name;
    private String images;
    private List<SubcategorySectionDTO> subcategories;
}
