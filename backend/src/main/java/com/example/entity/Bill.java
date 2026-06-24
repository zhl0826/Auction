package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Bill {
    private Long id;
    private Long userId;
    private String type;          // recharge / place_bid / bid_refund / cancel_refund / offshelf_refund / sold_income / sold_fee
    private BigDecimal amount;    // 正=收入, 负=支出
    private BigDecimal balanceAfter;
    private String relatedType;   // bid / goods / order / recharge
    private Long relatedId;
    private String memo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
