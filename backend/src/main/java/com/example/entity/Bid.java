package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Bid {
    private Long id;
    private Long goodsId;
    private String goodsTitle;      // 联表展示
    private Long bidderId;
    private String bidder;          // 联表展示用户名
    private BigDecimal price;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
