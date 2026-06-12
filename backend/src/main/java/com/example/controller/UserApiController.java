package com.example.controller;

import com.example.common.Result;
import com.example.entity.*;
import com.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-api")
public class UserApiController {

    @Autowired private UserService userService;
    @Autowired private GoodsService goodsService;
    @Autowired private BidService bidService;
    @Autowired private OrderService orderService;
    @Autowired private SysConfigService sysConfigService;

    // ==================== 辅助方法 ====================
    private User requireUser(Long userId) {
        if (userId == null) throw new RuntimeException("未登录");
        User u = userService.getById(userId);
        if (u == null) throw new RuntimeException("用户不存在");
        return u;
    }

    // ==================== 注册 ====================
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> body) {
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
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String account = body.get("account");
        String password = body.get("password");
        if (account == null || password == null) return Result.error(400, "请输入账号和密码");

        // 支持用户名或密码登录
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
            u.setPassword(null); // 不返回密码
            return Result.ok(u);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    // ==================== 充值 ====================
    @PutMapping("/balance/recharge")
    public Result<User> recharge(@RequestHeader("X-User-Id") Long userId,
                                  @RequestBody Map<String, Object> body) {
        try {
            User u = requireUser(userId);
            BigDecimal amount = BigDecimal.valueOf(((Number) body.get("amount")).doubleValue());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) return Result.error(400, "充值金额必须为正数");
            BigDecimal newBalance = u.getBalance().add(amount);
            User updated = userService.updateBalance(userId, newBalance);
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
                                   @RequestBody Goods goods) {
        try {
            User u = requireUser(userId);
            if (goods.getTitle() == null || goods.getTitle().trim().isEmpty()) return Result.error(400, "请输入商品名称");
            if (goods.getStartPrice() == null || goods.getStartPrice().compareTo(BigDecimal.ZERO) <= 0)
                return Result.error(400, "起拍价必须为正数");
            if (goods.getEndAt() == null) return Result.error(400, "请设置竞拍结束时间");

            goods.setSellerId(userId);
            // 获取系统配置中的最小加价幅度
            SysConfig cfg = sysConfigService.get();
            goods.setMinIncrement(cfg != null ? cfg.getMinIncrement() : BigDecimal.TEN);
            Goods result = goodsService.add(goods);
            return Result.ok(result);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ==================== 我的上架 ====================
    @GetMapping("/goods/my-goods")
    public Result<List<Goods>> myGoods(@RequestHeader("X-User-Id") Long userId) {
        try {
            requireUser(userId);
            return Result.ok(goodsService.listBySeller(userId));
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
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
            goodsService.offShelf(id);
            return Result.ok();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ==================== 竞拍出价 ====================
    @PostMapping("/bid/place")
    public Result<Goods> placeBid(@RequestHeader("X-User-Id") Long userId,
                                   @RequestBody Map<String, Object> body) {
        try {
            requireUser(userId);
            Long goodsId = ((Number) body.get("goodsId")).longValue();
            BigDecimal price = BigDecimal.valueOf(((Number) body.get("price")).doubleValue());
            Goods result = goodsService.placeBid(goodsId, userId, price);
            return Result.ok(result);
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
    @GetMapping("/order/{id}")
    public Result<Order> orderDetail(@PathVariable Long id) {
        Order o = orderService.findById(id);
        if (o == null) return Result.error(404, "订单不存在");
        return Result.ok(o);
    }
}
