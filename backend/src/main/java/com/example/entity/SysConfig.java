package com.example.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SysConfig {
    private Integer id;             // 固定 1
    private BigDecimal minIncrement;
    private BigDecimal feeRate;     // 0~1
}
