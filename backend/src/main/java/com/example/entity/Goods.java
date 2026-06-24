package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Goods {
    private Long id;
    private String title;
    private String type;
    private Long sellerId;
    private String sellerName;
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private BigDecimal minIncrement;
    private String cover;
    private String description;
    private String status;
    private String rejectReason;
    private Long currentBidderId;     // 当前最高出价人
    private String currentBidderName; // 当前最高出价人昵称
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
