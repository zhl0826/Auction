package com.example.service.impl;

import com.example.entity.AfterSale;
import com.example.entity.Bill;
import com.example.entity.Order;
import com.example.entity.User;
import com.example.mapper.AfterSaleMapper;
import com.example.mapper.BillMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.UserMapper;
import com.example.service.AfterSaleService;
import com.example.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AfterSaleServiceImpl implements AfterSaleService {
    @Autowired private AfterSaleMapper mapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private BillService billService;

    @Override public List<AfterSale> list() { return mapper.list(); }
    @Override public AfterSale findById(Long id) { return mapper.findById(id); }
    @Override public int insert(AfterSale a) { return mapper.insert(a); }

    /**
     * 同意退款: 买家收到 amount - fee, 卖家扣 amount - fee, 手续费平台收走
     * - 买家获得: amount - fee (从原成交价扣掉平台手续费)
     * - 卖家损失: amount - fee (从余额扣掉)
     * - 平台手续费 fee 留作平台收入
     * - order.status -> refunded, order.after_sale_status -> refunded
     */
    @Override
    @Transactional
    public void refund(Long id) {
        AfterSale as = mapper.findById(id);
        if (as == null) throw new RuntimeException("售后单不存在");
        if (!"pending".equals(as.getStatus())) throw new RuntimeException("该售后单已处理");

        // 通过 order_id 找到订单
        Order order = orderMapper.findById(as.getOrderId());
        if (order == null) throw new RuntimeException("订单不存在");
        if (!"paid".equals(order.getStatus())) throw new RuntimeException("订单状态不允许退款");

        BigDecimal amount = order.getAmount() == null ? BigDecimal.ZERO : order.getAmount();
        BigDecimal fee = order.getFee() == null ? BigDecimal.ZERO : order.getFee();
        BigDecimal net = amount.subtract(fee); // 净额 = amount - fee

        // 1) 买家 +net
        User buyer = userMapper.findById(order.getBuyerId());
        if (buyer != null) {
            userMapper.updateBalance(buyer.getId(), buyer.getBalance().add(net));
            billService.record(buyer.getId(), "after_sale_refund", net, "after_sale", id,
                "售后退款 (订单: " + order.getId() + ")");
        }
        // 2) 卖家 -net (即从卖家余额扣掉)
        User seller = userMapper.findById(order.getSellerId());
        if (seller != null) {
            userMapper.updateBalance(seller.getId(), seller.getBalance().subtract(net));
            billService.record(seller.getId(), "after_sale_payback", net.negate(), "after_sale", id,
                "售后扣款 (订单: " + order.getId() + ")");
        }

        // 3) 更新订单状态
        orderMapper.updateStatus(order.getId(), "refunded");
        orderMapper.updateAfterSaleStatus(order.getId(), "refunded");
        // 4) 更新售后单状态
        mapper.updateStatus(id, "refunded");
    }

    /**
     * 驳回: 状态改为 rejected, 订单维持 paid
     */
    @Override
    @Transactional
    public void reject(Long id) {
        AfterSale as = mapper.findById(id);
        if (as == null) throw new RuntimeException("售后单不存在");
        if (!"pending".equals(as.getStatus())) throw new RuntimeException("该售后单已处理");
        Order order = orderMapper.findById(as.getOrderId());
        if (order != null) {
            orderMapper.updateAfterSaleStatus(order.getId(), "rejected");
        }
        mapper.updateStatus(id, "rejected");
    }
}
