package com.example.service;

import com.example.entity.AfterSale;
import java.util.List;

public interface AfterSaleService {
    List<AfterSale> list();
    void refund(Long id);
    void reject(Long id);
}
