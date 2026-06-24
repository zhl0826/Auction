package com.example.service;

import com.example.entity.Bill;
import java.math.BigDecimal;
import java.util.List;

public interface BillService {
    int insert(Bill bill);
    List<Bill> listByUser(Long userId, String type, Integer limit);
    /** 记录一条流水(简化:由调用方准备好 Bill 对象) */
    void record(Long userId, String type, BigDecimal amount, String relatedType, Long relatedId, String memo);
}
