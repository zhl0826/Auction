package com.example.service.impl;

import com.example.entity.Goods;
import com.example.entity.Bid;
import com.example.entity.Order;
import com.example.entity.User;
import com.example.service.BillService;
import com.example.service.SysConfigService;
import com.example.mapper.GoodsMapper;
import com.example.mapper.BidMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.OrderMapper;
import com.example.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired private GoodsMapper mapper;
    @Autowired private BidMapper bidMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private BillService billService;
    @Autowired private SysConfigService sysConfigService;


    /**
     * 退还一笔出价(幂等): 同一笔 bid 多次调用只退一次.
     * 退完把 status 置为 cancelled, refunded 置为 true.
     */
    private void refundBid(Bid b) {
        if (b == null) return;
        if (Boolean.TRUE.equals(b.getRefunded())) return; // 幂等
        if (!"active".equals(b.getStatus())) return;       // 已经被超越 / 中标 / 已取消的不再退
        User u = userMapper.findById(b.getBidderId());
        if (u != null) {
            userMapper.updateBalance(b.getBidderId(), u.getBalance().add(b.getPrice()));
        }
        bidMapper.updateStatus(b.getId(), "cancelled");
        bidMapper.updateRefunded(b.getId(), true);
        b.setRefunded(true);
        b.setStatus("cancelled");
        // 写流水 (退款 = 收入)
        billService.record(b.getBidderId(), "bid_refund", b.getPrice(), "bid", b.getId(), "出价退款 (商品: " + b.getGoodsId() + ")");
    }

    @Override public List<Goods> listByStatus(String status, String keyword, String type) {
        return mapper.listByStatus(status, keyword, type);
    }
    @Override public void approve(Long id) { mapper.updateStatus(id, "on_sale"); }
    @Override public void reject(Long id, String reason) { mapper.updateReject(id, reason); }

    @Override
    public void offShelf(Long id) {
        offShelf(id, false);
    }

    /**
     * 下架商品(支持是否退款)
     * @param id 商品 ID
     * @param refundBids true=退还所有未中标出价者的钱,false=不退
     */
    @Override
    @Transactional
    public void offShelf(Long id, boolean refundBids) {
        Goods g = mapper.findById(id);
        if (g == null) throw new RuntimeException("商品不存在");
        // 标记商品下架
        mapper.updateStatus(id, "off_shelf");
        if (refundBids) {
            for (Bid b : bidMapper.listByGoods(id)) {
                refundBid(b);
            }
        }
    }

    @Override public Goods detail(Long id) { return mapper.findById(id); }
    @Override
    public List<Goods> listActive(String keyword, String type) {
        return mapper.listActive(keyword, type);
    }
    @Override
    public Goods add(Goods goods) {
        goods.setCurrentPrice(goods.getStartPrice());
        goods.setStatus("pending");
        mapper.insert(goods);
        return goods;
    }
    @Override
    public List<Goods> listBySeller(Long sellerId) {
        return mapper.listBySeller(sellerId);
    }
    @Override
    public List<Goods> listBySellerAndStatus(Long sellerId, String status, String keyword) {
        return mapper.listByStatusIncludingRemoved(sellerId, keyword, status);
    }
    @Override
    @Transactional
    public void relist(Long id) {
        Goods g = mapper.findById(id);
        if (g == null) throw new RuntimeException("商品不存在");
        if (!"off_shelf".equals(g.getStatus())) throw new RuntimeException("只能重新上架已下架的商品");
        // 重新上架:把价格重置为起拍价,清空出价人,重置结束时间(7天后)
        mapper.updatePrice(id, g.getStartPrice());
        mapper.updateCurrentBidder(id, null);
        mapper.updateEndAt(id, LocalDateTime.now().plusDays(7));
        mapper.updateStatus(id, "on_sale");
        for (Bid b : bidMapper.listByGoods(id)) {
            refundBid(b);
        }
    }
    @Override
    @Transactional
    public void delete(Long id) {
        Goods g = mapper.findById(id);
        if (g == null) throw new RuntimeException("商品不存在");
        if ("on_sale".equals(g.getStatus())) throw new RuntimeException("请先下架商品再删除");
        for (Bid b : bidMapper.listByGoods(id)) {
            refundBid(b);
        }
        // 真正删除商品
        mapper.deleteById(id);
    }

    @Override
    @Transactional
    public Goods placeBid(Long goodsId, Long userId, BigDecimal price) {
        Goods g = mapper.findById(goodsId);
        if (g == null) throw new RuntimeException("商品不存在");
        if (!"on_sale".equals(g.getStatus())) throw new RuntimeException("商品不在竞拍中");
        if (g.getEndAt() != null && g.getEndAt().isBefore(LocalDateTime.now())) throw new RuntimeException("竞拍已结束");
        if (g.getSellerId().equals(userId)) throw new RuntimeException("不能竞拍自己的商品");

        // 关键:最低加价幅度校验(用 min_increment 字段)
        BigDecimal minRequired = g.getCurrentPrice().add(g.getMinIncrement());
        if (price.compareTo(minRequired) < 0) {
            throw new RuntimeException("出价必须 >= 当前价 + 加价幅度,即至少 ¥" + minRequired);
        }

        User bidder = userMapper.findById(userId);
        if (bidder == null) throw new RuntimeException("用户不存在");
        if (bidder.getBalance().compareTo(price) < 0) throw new RuntimeException("余额不足");

        // 1) 退还前一个最高出价者的钱(如果存在,且不是自己)
        if (g.getCurrentBidderId() != null && !g.getCurrentBidderId().equals(userId)) {
            // 找到前一个 active 的出价(同 goodsId,同 bidder)
            List<Bid> prevBids = bidMapper.listByGoods(goodsId);
            for (Bid pb : prevBids) {
                if (pb.getBidderId().equals(g.getCurrentBidderId())
                    && "active".equals(pb.getStatus())) {
                    refundBid(pb);
                    // 该笔是"被超越"而非"取消",改回 outbid 状态
                    bidMapper.updateStatus(pb.getId(), "outbid");
                    pb.setStatus("outbid");
                    break;
                }
            }
        }

        // 2) 如果是同一个买家连续加价(已经是 currentBidder),不再扣款
        if (g.getCurrentBidderId() != null && g.getCurrentBidderId().equals(userId)) {
            // 找上一笔 active 出价
            List<Bid> myBids = bidMapper.listByGoods(goodsId);
            BigDecimal prevPrice = BigDecimal.ZERO;
            for (Bid mb : myBids) {
                if (mb.getBidderId().equals(userId)
                    && "active".equals(mb.getStatus())) {
                    prevPrice = mb.getPrice();
                    bidMapper.updateStatus(mb.getId(), "outbid");
                    break;
                }
            }
            // 差额扣款
            BigDecimal diff = price.subtract(prevPrice);
            userMapper.updateBalance(userId, bidder.getBalance().subtract(diff));
            billService.record(userId, "place_bid", diff.negate(), "bid", null, "加价差额: " + diff.toPlainString() + " (商品: " + goodsId + ")");
        } else {
            // 全新买家,全额扣款
            userMapper.updateBalance(userId, bidder.getBalance().subtract(price));
            billService.record(userId, "place_bid", price.negate(), "bid", null, "首次出价: " + price.toPlainString() + " (商品: " + goodsId + ")");
        }

        // 3) 更新商品当前价 + 当前出价人
        mapper.updatePrice(goodsId, price);
        mapper.updateCurrentBidder(goodsId, userId);

        // 4) 记录新出价
        Bid bid = new Bid();
        bid.setGoodsId(goodsId);
        bid.setBidderId(userId);
        bid.setPrice(price);
        bid.setStatus("active");
        bid.setRefunded(false);
        bidMapper.insert(bid);
        return mapper.findById(goodsId);
    }

    @Override
    @Transactional
    public void cancelBid(Long bidId, Long userId) {
        Bid bid = bidMapper.findById(bidId);
        if (bid == null) throw new RuntimeException("出价记录不存在");
        if (!bid.getBidderId().equals(userId)) throw new RuntimeException("只能取消自己的出价");
        if (!"active".equals(bid.getStatus())) throw new RuntimeException("该出价已结束");
        if (Boolean.TRUE.equals(bid.getRefunded())) throw new RuntimeException("已退款");

        refundBid(bid);

        // 如果该出价是商品的当前最高价,需要清空商品的 currentBidder
        Goods g = mapper.findById(bid.getGoodsId());
        if (g != null && userId.equals(g.getCurrentBidderId())) {
            // 找次高出价作为新 currentBidder
            List<Bid> bids = bidMapper.listByGoods(bid.getGoodsId());
            Long newBidder = null;
            BigDecimal newPrice = g.getStartPrice();
            for (Bid b : bids) {
                if ("active".equals(b.getStatus()) && Boolean.FALSE.equals(b.getRefunded())) {
                    newBidder = b.getBidderId();
                    newPrice = b.getPrice();
                    break;
                }
            }
            mapper.updatePrice(bid.getGoodsId(), newPrice);
            mapper.updateCurrentBidder(bid.getGoodsId(), newBidder);
        }
    }

    @Override
    public java.util.List<com.example.entity.Bid> listBids(Long goodsId) {
        return bidMapper.listByGoods(goodsId);
    }

    /**
     * 结束拍卖并成交 (卖家主动调用)
     * - 如果有最高出价者: 生成订单, 状态 sold, 最高出价 bid.status = won
     * - 其他 active 出价: 全部退款 + 状态 cancelled
     * - 商品状态 -> sold
     */
    @Override
    @Transactional
    public void closeAuction(Long goodsId, Long userId) {
        Goods g = mapper.findById(goodsId);
        if (g == null) throw new RuntimeException("商品不存在");
        if (!g.getSellerId().equals(userId)) throw new RuntimeException("只能结束自己的商品");
        if (!"on_sale".equals(g.getStatus())) throw new RuntimeException("商品不在竞拍中,无法成交");
        doClose(g, true);
    }

    /**
     * 时间到期自动结束 (无需校验卖家身份, 系统调用)
     * - 有出价 -> 成交, 同上
     * - 无出价 -> 状态 finished
     */
    @Override
    @Transactional
    public void finishExpired(Long goodsId) {
        Goods g = mapper.findById(goodsId);
        if (g == null) return;
        if (!"on_sale".equals(g.getStatus())) return;
        if (g.getEndAt() == null) return;
        if (g.getEndAt().isAfter(java.time.LocalDateTime.now())) return; // 未到期
        doClose(g, true);
    }

    /**
     * 共用结束逻辑 (供 closeAuction 和 finishExpired 调用)
     */
        private void doClose(Goods g, boolean createOrder) {
        Long goodsId = g.getId();
        java.util.List<com.example.entity.Bid> bids = bidMapper.listByGoods(goodsId);
        // 找出所有 active 未退款的出价
        com.example.entity.Bid topBid = null;
        for (com.example.entity.Bid b : bids) {
            if ("active".equals(b.getStatus()) && Boolean.FALSE.equals(b.getRefunded())) {
                if (topBid == null || b.getPrice().compareTo(topBid.getPrice()) > 0) {
                    topBid = b;
                }
            }
        }
        if (topBid == null) {
            // 无人出价 -> 直接下架(不下架退款,因为没出价)
            mapper.updateStatus(goodsId, "off_shelf");
            return;
        }
        // 把 topBid 标记为 won
        
        // 防御: 如果最高出价人就是卖家(placeBid已拦截,此处是兑不万寿避免被用户过滤)
        if (topBid.getBidderId().equals(g.getSellerId())) {
            // 退还他自己的出价 + 下架商品
            refundBid(topBid);
            mapper.updateStatus(goodsId, "off_shelf");
            return;
        }
bidMapper.updateStatus(topBid.getId(), "won");
        topBid.setStatus("won");
        // 其他 active 出价全部退款 (refundBid 内部写流水)
        for (com.example.entity.Bid b : bids) {
            if (b.getId().equals(topBid.getId())) continue;
            if ("active".equals(b.getStatus()) && Boolean.FALSE.equals(b.getRefunded())) {
                refundBid(b);
            }
        }
        // 商品状态 -> sold
        mapper.updateStatus(goodsId, "sold");
        // 计算手续费 + 卖家收款
        java.math.BigDecimal price = topBid.getPrice();
        java.math.BigDecimal feeRate = java.math.BigDecimal.ZERO;
        try {
            com.example.entity.SysConfig cfg = sysConfigService.get();
            if (cfg != null && cfg.getFeeRate() != null) feeRate = cfg.getFeeRate();
        } catch (Exception ignore) {}
        java.math.BigDecimal fee = price.multiply(feeRate).setScale(2, java.math.RoundingMode.HALF_UP);
        java.math.BigDecimal sellerIncome = price.subtract(fee);
        // 给卖家加余额
        User seller = userMapper.findById(g.getSellerId());
        if (seller != null) {
            userMapper.updateBalance(seller.getId(), seller.getBalance().add(sellerIncome));
            billService.record(seller.getId(), "sold_income", sellerIncome, "order", goodsId,
                "卖出商品收入 (商品: " + goodsId + ")");
        }
        // 生成订单
        if (createOrder) {
            com.example.entity.Order o = new com.example.entity.Order();
            o.setGoodsId(goodsId);
            o.setGoodsTitle(g.getTitle());
            o.setBuyerId(topBid.getBidderId());
            o.setSellerId(g.getSellerId());
            o.setAmount(price);
            o.setFee(fee);
            o.setStatus("paid");
            orderMapper.insert(o);
        }
    }public int checkAllExpired() {
        java.util.List<com.example.entity.Goods> onSale = mapper.listByStatus("on_sale", null, null);
        int n = 0;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (com.example.entity.Goods g : onSale) {
            if (g.getEndAt() != null && !g.getEndAt().isAfter(now)) {
                try {
                    finishExpired(g.getId());
                    n++;
                } catch (RuntimeException ignore) {}
            }
        }
        return n;
    }

}
