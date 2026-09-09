package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.dto.AiChatRequest;
import com.vericart.dto.AiChatResponse;
import com.vericart.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/analyze/{productId}")
    public Result<Map<String, Object>> analyzeProduct(@PathVariable Long productId) {
        return Result.success(aiService.analyzeProduct(productId));
    }

    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@RequestBody AiChatRequest request,
                                        @RequestParam(value = "userId", required = false) Long userId) {
        if (request.getProductId() == null && request.getQuestion() != null) {
            return Result.success(aiService.generalChat(userId, request.getQuestion(), request.getHistory()));
        }
        return Result.success(aiService.chat(userId, request));
    }

    @PostMapping("/batch")
    public Result<String> batchAnalyze() {
        aiService.batchAnalyzePending();
        return Result.success("Batch analysis started");
    }

    @PostMapping("/recommend")
    public Result<Map<String, Object>> recommend(@RequestBody Map<String, String> body,
                                                  @RequestParam(value = "userId", required = false) Long userId) {
        String preferences = body.getOrDefault("preferences", "");
        return Result.success(aiService.recommend(userId, preferences));
    }
}
