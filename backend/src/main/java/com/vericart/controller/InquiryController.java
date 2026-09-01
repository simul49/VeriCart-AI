package com.vericart.controller;

import com.vericart.common.Result;
import com.vericart.dto.MessageRequest;
import com.vericart.entity.Inquiry;
import com.vericart.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Product inquiries: customers message the store owner, seller replies in the dashboard. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    /** Customer sends a message about a product to the store owner. */
    @PostMapping("/messages")
    public Result<Inquiry> send(@RequestBody MessageRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success("Message sent", inquiryService.send(userId, request.getProductId(), request.getMessage()));
    }

    /** Customer sends a follow-up message in an existing conversation (multi-turn chat). */
    @PostMapping("/messages/{id}/send")
    public Result<Inquiry> sendFollowUp(@PathVariable Long id,
                                        @RequestBody Map<String, String> body,
                                        Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success("Message sent", inquiryService.sendFollowUp(id, userId, body.get("message")));
    }

    /** Customer's own message history with seller replies. */
    @GetMapping("/messages/my")
    public Result<List<Map<String, Object>>> myMessages(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(inquiryService.myMessages(userId));
    }

    /** Store owner inbox (SELLER/ADMIN). */
    @GetMapping("/seller/messages")
    public Result<List<Map<String, Object>>> sellerInbox(Authentication auth) {
        Long sellerId = (Long) auth.getPrincipal();
        return Result.success(inquiryService.sellerInbox(sellerId));
    }

    @GetMapping("/seller/messages/unread-count")
    public Result<Integer> sellerUnreadCount(Authentication auth) {
        Long sellerId = (Long) auth.getPrincipal();
        return Result.success(inquiryService.sellerUnreadCount(sellerId));
    }

    /** Store owner replies to a customer inquiry. */
    @PutMapping("/seller/messages/{id}/reply")
    public Result<Inquiry> reply(@PathVariable Long id,
                                 @RequestBody Map<String, String> body,
                                 Authentication auth) {
        Long sellerId = (Long) auth.getPrincipal();
        return Result.success("Reply sent", inquiryService.reply(id, sellerId, body.get("reply")));
    }

    @PutMapping("/seller/messages/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, Authentication auth) {
        Long sellerId = (Long) auth.getPrincipal();
        inquiryService.markRead(id, sellerId);
        return Result.success("Marked as read", null);
    }
}
