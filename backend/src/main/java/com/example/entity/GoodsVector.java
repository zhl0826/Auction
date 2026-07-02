package com.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GoodsVector {
    private Long goodsId;
    private String embedding;
    private LocalDateTime updatedAt;
}