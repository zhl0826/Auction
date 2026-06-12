package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AfterSale {
    private Long id;
    private Long orderId;
    private String goodsTitle;
    private String buyer;
    private String seller;
    private String reason;
    private String status;          // pending / refunded / rejected
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}
