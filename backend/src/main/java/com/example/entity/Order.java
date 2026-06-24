package com.example.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private Long goodsId;
    private String goodsTitle;
    private String goodsCover;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private BigDecimal amount;
    private BigDecimal fee;     // 平台手续费
    private String status;
    private String afterSaleStatus; // none / pending / refunded / rejected
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}