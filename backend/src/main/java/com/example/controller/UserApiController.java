package com.example.controller;

import com.example.common.Result;
import com.example.entity.*;
import com.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-api")
public class UserApiController {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }

    @Autowired private UserService userService;
    @Autowired private GoodsService goodsService;
    @Autowired private BidService bidService;
    @Autowired private OrderService orderService;
    @Autowired private SysConfigService sysConfigService;
    @Autowired private com.example.service.BillService billService;
    @Autowired private AfterSaleService afterSaleService;
    @Autowired private com.example.mapper.OrderMapper orderMapper;

    // ==================== 辅助方法 ====================
    private User requireUser(Long userId) {
        if (userId == null) throw new RuntimeException("未登录");
        User u = userService.getById(userId);
        if (u == null) throw new RuntimeException("用户不存在");
        return u;
    }

    // ==================== 注册 ====================
    @PostMapping("/register")
    public Result<Map<String, Object>> register(HttpEntity<String> entity) throws Exception {
        Map<String, String> body = MAPPER.readValue(stripBom(entity.getBody()), new TypeReference<Map<String, String>>(){});
        String username = body.get("username");
        String password = body.get("password");
        String nickname = body.get("nickname");
        String phone = body.get("phone");
        if (username == null || username.trim().isEmpty()) return Result.error(400, "请输入用户名");
        if (password == null || password.length() < 6) return Result.error(400, "密码至少6位");
        if (phone == null || !phone.matches("^1\\d{10}$")) return Result.error(400, "手机号格式不正确");

        try {
            User u = userService.register(username.trim(), password, nickname != null ? nickname : username.trim(), phone.trim());
            Map<String, Object> data = new HashMap<>();
            data.put("userId", u.getId());
            data.put("username", u.getUsername());
            data.put("nickname", u.getNickname());
            data.put("token", "user-token-" + u.getId());
            return Result.ok(data);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ==================== 登录 ====================
    @PostMapping("/login")
    public Result<Map<String, Object>> login(HttpEntity<String> entity) throws Exception {
        Map<String, String> body = MAPPER.readValue(stripBom(entity.getBody()), new TypeReference<Map<String, String>>(){});
        String account = body.get("account");
        String password = body.get("password");
        if (account == null || password == null) return Result.error(400, "请输入账号和密码");

        try {
            User u = userService.login(account.trim(), password);
            Map<String, Object> data = new HashMap<>();
            data.put("userId", u.getId());
            data.put("username", u.getUsername());
            data.put("nickname", u.getNickname());
            data.put("phone", "");
            data.put("balance", u.getBalance());
            data.put("token", "user-token-" + u.getId());
            return Result.ok(data);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    // ==================== 个人信息 ====================
    @GetMapping("/profile")
    public Result<User> profile(@RequestHeader("X-User-Id") Long userId) {
        try {
            User u = requireUser(userId);
            u.setPassword(null);
            return Result.ok(u);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    // ==================== 充值 ====================
    @PutMapping("/balance/recharge")
    public Result<User> recharge(@RequestHeader("X-User-Id") Long userId, HttpEntity<String> entity) throws Exception {
        Map<String, Object> body = MAPPER.readValue(stripBom(entity.getBody()), new TypeReference<Map<String, Object>>(){});
        try {
            User u = requireUser(userId);
            BigDecimal amount = BigDecimal.valueOf(((Number) body.get("amount")).doubleValue());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) return Result.error(400, "充值金额必须为正数");
            BigDecimal newBalance = u.getBalance().add(amount);
            User updated = userService.recharge(userId, amount);
            updated.setPassword(null);
            return Result.ok(updated);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    // ==================== 在售商品列表 ====================
    @GetMapping("/goods/list")
    public Result<List<Goods>> goodsList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String type) {
        return Result.ok(goodsService.listActive(keyword, type));
    }

    // ==================== 商品详情 ====================
    @GetMapping("/goods/{id}")
    public Result<Goods> goodsDetail(@PathVariable Long id) {
        Goods g = goodsService.detail(id);
        if (g == null) return Result.error(404, "商品不存在");
        return Result.ok(g);
    }

    // ==================== 上架商品 ====================
    @PostMapping("/goods/add")
    public Result<Goods> addGoods(@RequestHeader("X-User-Id") Long userId,
                                   HttpEntity<String> entity) throws Exception {
        try {
            User u = requireUser(userId);
            Goods goods = MAPPER.readValue(stripBom(entity.getBody()), Goods.class);
            if (goods.getTitle() == null || goods.getTitle().trim().isEmpty()) return Result.error(400, "请输入商品名称");
            if (goods.getStartPrice() == null || goods.getStartPrice().compareTo(BigDecimal.ZERO) <= 0)
                return Result.error(400, "起拍价必须为正数");
            if (goods.getEndAt() == null) return Result.error(400, "请设置竞拍结束时间");

            goods.setSellerId(userId);
            SysConfig cfg = sysConfigService.get();
            goods.setMinIncrement(cfg != null ? cfg.getMinIncrement() : BigDecimal.TEN);
            Goods result = goodsService.add(goods);
            return Result.ok(result);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ==================== 我的上架(含已下架) ====================
    @GetMapping("/goods/my-goods-all")
    public Result<List<Goods>> myGoodsAll(@RequestHeader("X-User-Id") Long userId,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) String keyword) {
        try {
            return Result.ok(goodsService.listBySellerAndStatus(userId, status, keyword));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ==================== 我的上架 ====================
    @GetMapping("/goods/my-goods")
    public Result<List<Goods>> myGoods(@RequestHeader("X-User-Id") Long userId) {
        try {
            requireUser(userId);
            return Result.ok(goodsService.listBySellerAndStatus(userId, "on_sale", null));
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    // ==================== 重新上架 ====================
    @PutMapping("/goods/{id}/relist")
    public Result<Void> relist(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        try {
            goodsService.detail(id); // exists check
            goodsService.relist(id);
            return Result.ok();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ==================== 删除 ====================
    @DeleteMapping("/goods/{id}")
    public Result<Void> deleteGoods(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        try {
            goodsService.delete(id);
            return Result.ok();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ==================== 下架 ====================
    @PutMapping("/goods/{id}/off-shelf")
    public Result<Void> offShelf(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        try {
            User u = requireUser(userId);
            Goods g = goodsService.detail(id);
            if (g == null) return Result.error(404, "商品不存在");
            if (!g.getSellerId().equals(userId)) return Result.error(403, "只能下架自己的商品");
            goodsService.offShelf(id, true); // 下架时直接退还所有买家出价
            return Result.ok();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ==================== 竞拍出价 ====================
    @PostMapping("/bid/place")
    public Result<Goods> placeBid(@RequestHeader("X-User-Id") Long userId, HttpEntity<String> entity) throws Exception {
        try {
            requireUser(userId);
            Map<String, Object> body = MAPPER.readValue(stripBom(entity.getBody()), new TypeReference<Map<String, Object>>(){});
            Long goodsId = toLong(body.get("goodsId"));
            BigDecimal price = toBigDecimal(body.get("price"));
            Goods result = goodsService.placeBid(goodsId, userId, price);
            return Result.ok(result);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
    // ===== 类型安全转换 helper =====
    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        if (o instanceof String) {
            String s = ((String) o).trim();
            if (s.isEmpty()) return null;
            try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        if (o instanceof String) {
            String s = ((String) o).trim();
            if (s.isEmpty()) return null;
            try { return new BigDecimal(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }



    // ==================== 拍卖结束 / 成交 ====================
    @GetMapping("/goods/{id}/bids")
    public Result<java.util.List<com.example.entity.Bid>> listBids(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        try {
            requireUser(userId);
            Goods g = goodsService.detail(id);
            if (g == null) return Result.error(404, "商品不存在");
            return Result.ok(goodsService.listBids(id));
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/goods/{id}/close")
    public Result<com.example.entity.Order> closeAuction(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        try {
            requireUser(userId);
            goodsService.closeAuction(id, userId);
            // 返回新生成的订单
            com.example.entity.Order o = orderService.findByGoodsIdAndBuyer(id, goodsService.detail(id).getCurrentBidderId());
            return Result.ok(o);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/goods/check-expired")
    public Result<java.util.Map<String,Object>> checkExpired() {
        int n = goodsService.checkAllExpired();
        java.util.Map<String,Object> r = new java.util.HashMap<>();
        r.put("closed", n);
        return Result.ok(r);
    }

    // ==================== 账单流水 ====================
    @GetMapping("/bill/list")
    public Result<java.util.List<com.example.entity.Bill>> listBills(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer limit) {
        try {
            requireUser(userId);
            int lim = (limit == null || limit <= 0) ? 200 : Math.min(limit, 1000);
            return Result.ok(billService.listByUser(userId, type, lim));
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    // ==================== 取消出价 ====================
    @PostMapping("/bid/cancel")
    public Result<Void> cancelBid(@RequestHeader("X-User-Id") Long userId, HttpEntity<String> entity) throws Exception {
        try {
            Map<String, Object> body = MAPPER.readValue(stripBom(entity.getBody()), new TypeReference<Map<String, Object>>(){});
            Long bidId = toLong(body.get("bidId"));
            goodsService.cancelBid(bidId, userId);
            return Result.ok();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ==================== 我的竞拍记录 ====================
    @GetMapping("/bid/my-bids")
    public Result<List<Bid>> myBids(@RequestHeader("X-User-Id") Long userId) {
        try {
            requireUser(userId);
            return Result.ok(bidService.listByUserId(userId));
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    // ==================== 购买订单 ====================
    @GetMapping("/order/purchases")
    public Result<List<Order>> purchases(@RequestHeader("X-User-Id") Long userId) {
        try {
            requireUser(userId);
            return Result.ok(orderService.listByBuyer(userId));
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    // ==================== 卖出订单 ====================
    @GetMapping("/order/sales")
    public Result<List<Order>> sales(@RequestHeader("X-User-Id") Long userId) {
        try {
            requireUser(userId);
            return Result.ok(orderService.listBySeller(userId));
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    // ==================== 订单详情 ====================

    // ==================== 申请售后 ====================
    @PostMapping("/order/{id}/apply-refund")
    public Result<com.example.entity.AfterSale> applyRefund(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            HttpEntity<String> entity) throws Exception {
        try {
            User u = requireUser(userId);
            com.example.entity.Order order = orderService.findById(id);
            if (order == null) return Result.error(404, "订单不存在");
            if (!order.getBuyerId().equals(userId)) return Result.error(403, "只能申请自己的订单");
            if (!"paid".equals(order.getStatus())) return Result.error(400, "只能申请已付款订单");
            if (order.getAfterSaleStatus() != null && !"none".equals(order.getAfterSaleStatus())) {
                return Result.error(400, "该订单已申请过售后");
            }
            String reason = "";
            try {
                java.util.Map<String, Object> body = MAPPER.readValue(stripBom(entity.getBody()), new TypeReference<java.util.Map<String, Object>>(){});
                Object r = body.get("reason");
                if (r != null) reason = String.valueOf(r);
            } catch (Exception ignore) {}
            AfterSale as = new AfterSale();
            as.setOrderId(id);
            as.setReason(reason);
            as.setStatus("pending");
            afterSaleService.insert(as);
            orderMapper.updateAfterSaleStatus(id, "pending");
            return Result.ok(as);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/order/{id}")
    public Result<Order> orderDetail(@PathVariable Long id) {
        Order o = orderService.findById(id);
        if (o == null) return Result.error(404, "订单不存在");
        return Result.ok(o);
    }
}