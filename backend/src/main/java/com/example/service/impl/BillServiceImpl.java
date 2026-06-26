package com.example.service.impl;

import com.example.entity.Bill;
import com.example.entity.User;
import com.example.mapper.BillMapper;
import com.example.mapper.UserMapper;
import com.example.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class BillServiceImpl implements BillService {
    @Autowired private BillMapper billMapper;
    @Autowired private UserMapper userMapper;

    @Override public int insert(Bill bill) { return billMapper.insert(bill); }

    @Override public List<Bill> listByUser(Long userId, String type, Integer limit) {
        return billMapper.listByUser(userId, type, limit);
    }

    /**
     * 写一条流水. 假设余额已经被外部 update, 这里只查最新余额并记录.
     * amount: 正=收入, 负=支出
     */
    @Override
    @Transactional
    public void record(Long userId, String type, BigDecimal amount, String relatedType, Long relatedId, String memo) {
        User u = userMapper.findById(userId);
        if (u == null) return;
        Bill b = new Bill();
        b.setUserId(userId);
        b.setType(type);
        b.setAmount(amount);
        b.setBalanceAfter(u.getBalance());
        b.setRelatedType(relatedType);
        b.setRelatedId(relatedId);
        b.setMemo(memo);
        billMapper.insert(b);
    }
}
