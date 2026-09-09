package com.vericart.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class AiChatRequest {
    private Long productId;
    private String question;
    // Prior conversation turns (role: "user" | "assistant", content) for multi-turn chat.
    private List<Map<String, String>> history;
}
