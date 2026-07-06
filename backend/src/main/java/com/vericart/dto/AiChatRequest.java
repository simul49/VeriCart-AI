package com.vericart.dto;

import lombok.Data;

@Data
public class AiChatRequest {
    private Long productId;
    private String question;
}
