package com.vericart.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** One chat turn inside an inquiry thread (multi-turn conversation). */
@Data
public class InquiryMessage {
    private Long id;
    private Long inquiryId;
    /** USER / AI / SELLER */
    private String sender;
    private String content;
    /** 1 = the other side has seen it */
    private Integer isRead;
    private LocalDateTime createdAt;
}
