package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Bid {
    private Long id;
    private Long goodsId;
    private String goodsTitle;
    private Long bidderId;
    private String bidder;
    private BigDecimal price;
    private String status;            // active / outbid / won / cancelled
    private Boolean refunded;         // 是否已退款
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
