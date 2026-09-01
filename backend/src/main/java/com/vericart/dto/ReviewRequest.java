package com.vericart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull
    private Long productId;

    /**
     * Optional. When present the review is treated as a verified purchase
     * (see Review.verified), which feeds the Trust Score.
     */
    private Long orderId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 2000)
    private String content;

    private String images;
}
