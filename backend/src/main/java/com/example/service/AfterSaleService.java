package com.example.service;

import com.example.entity.AfterSale;
import java.util.List;

public interface AfterSaleService {
    List<AfterSale> list();
    AfterSale findById(Long id);
    int insert(AfterSale a);
    void refund(Long id);
    void reject(Long id);
}
