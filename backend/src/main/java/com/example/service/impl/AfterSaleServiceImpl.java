package com.example.service.impl;

import com.example.entity.AfterSale;
import com.example.mapper.AfterSaleMapper;
import com.example.service.AfterSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AfterSaleServiceImpl implements AfterSaleService {
    @Autowired private AfterSaleMapper mapper;
    @Override public List<AfterSale> list() { return mapper.list(); }
    @Override public void refund(Long id) { mapper.updateStatus(id, "refunded"); }
    @Override public void reject(Long id) { mapper.updateStatus(id, "rejected"); }
}
