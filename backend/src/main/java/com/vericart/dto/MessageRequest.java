package com.vericart.dto;

import lombok.Data;

/** Body for sending a message to the store owner about a product. */
@Data
public class MessageRequest {
    private Long productId;
    private String message;
}
