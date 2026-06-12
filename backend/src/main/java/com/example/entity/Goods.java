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
    private Long sellerId;          // 关联 user.id
    private String sellerName;      // 联表展示
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private BigDecimal minIncrement;
    private String cover;
    private String description;
    private String status;          // pending / on_sale / sold / off_shelf / rejected
    private String rejectReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
