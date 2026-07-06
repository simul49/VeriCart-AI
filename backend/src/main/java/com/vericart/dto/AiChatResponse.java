package com.vericart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiChatResponse {
    private String answer;
    private String model;
    private Long productId;
}
