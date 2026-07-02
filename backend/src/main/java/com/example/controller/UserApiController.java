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
    @Autowired private com.example.mapper.GoodsVectorMapper goodsVectorMapper;
    @Autowired private BidService bidService;
    @Autowired private OrderService orderService;
    @Autowired private SysConfigService sysConfigService;
    @Autowired private com.example.service.BillService billService;
    @Autowired private AfterSaleService afterSaleService;
    @Autowired private com.example.mapper.OrderMapper orderMapper;
    @Autowired private org.springframework.core.env.Environment env;
    // AI 估价对话 session 存储 (内存)
    private final java.util.Map<String, java.util.List<java.util.Map<String, String>>> chatSessions = new java.util.concurrent.ConcurrentHashMap<>();
    @Autowired private org.springframework.web.client.RestTemplate restTemplate;

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

    // ==================== AI 拍品描述生成 ====================
    @PostMapping("/goods/ai-description")
    public Result<String> aiDescription(@RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        String type = (String) body.get("type");
        String description = (String) body.get("description");
        Object priceObj = body.get("startPrice");
        boolean hasStartPrice = priceObj != null;
        String startPrice = hasStartPrice ? priceObj.toString() : "";

        if (title == null || title.trim().isEmpty()) {
            return Result.error(400, "请提供商品名称");
        }

        // 构建 Prompt
        boolean isPolish = description != null && !description.trim().isEmpty();
        String systemPrompt = "你是一个专业的拍卖品文案撰写助手。请用中文回复，语言生动有感染力，突出商品卖点和稀缺性，字数控制在80字以内，不要使用markdown格式。";
        String userPrompt;
        if (isPolish) {
            userPrompt = "请润色以下拍卖品介绍文案，使其更有吸引力、突出卖点：\n"
                + "商品名称：" + title + "\n"
                + "商品类型：" + (type != null ? type : "") + "\n"
                + (hasStartPrice ? "起拍价格：" + startPrice + "元\n" : "")
                + "原标题：" + description;
        } else {
            userPrompt = "请为以下拍卖品生成一段吸引人的商品介绍文案：\n"
                + "商品名称：" + title + "\n"
                + "商品类型：" + (type != null ? type : "") + "\n"
                + (hasStartPrice ? "起拍价格：" + startPrice + "元\n" : "");
        }

        try {
            // 调用通义千问 DashScope API（兼容 OpenAI 格式）
            String apiKey = env.getProperty("ai.dashscope.api-key");
            String model = env.getProperty("ai.dashscope.model", "qwen-plus");

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", model);

            java.util.List<java.util.Map<String, String>> messages = new java.util.ArrayList<>();
            java.util.Map<String, String> sysMsg = new java.util.HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);

            java.util.Map<String, String> userMsg = new java.util.HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);

            requestBody.put("messages", messages);

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> req =
                new org.springframework.http.HttpEntity<>(requestBody, headers);

            org.springframework.http.ResponseEntity<java.util.Map> resp = restTemplate.postForEntity(
                "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
                req,
                java.util.Map.class
            );

            java.util.Map respBody = resp.getBody();
            if (respBody != null && respBody.containsKey("choices")) {
                java.util.List choices = (java.util.List) respBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    java.util.Map choice = (java.util.Map) choices.get(0);
                    java.util.Map message = (java.util.Map) choice.get("message");
                    String aiText = (String) message.get("content");
                    return Result.ok(aiText.trim());
                }
            }
            return Result.error("AI 生成失败，请稍后重试");
        } catch (Exception e) {
            return Result.error(500, "AI 服务调用失败: " + e.getMessage());
        }
    }

    // ==================== AI 估价助手 (RAG + 向量检索) ====================

    /** 调用 DashScope embedding API 获取文本向量 */
    private java.util.List<Double> getEmbedding(String text) {
        String apiKey = env.getProperty("ai.dashscope.api-key");
        if (apiKey == null || apiKey.isEmpty()) throw new RuntimeException("AI API Key 未配置");
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", "text-embedding-v3");
            requestBody.put("input", text);

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> req =
                new org.springframework.http.HttpEntity<>(requestBody, headers);

            org.springframework.http.ResponseEntity<java.util.Map> resp = restTemplate.postForEntity(
                "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings",
                req,
                java.util.Map.class
            );

            java.util.Map respBody = resp.getBody();
            if (respBody != null && respBody.containsKey("data")) {
                java.util.List data = (java.util.List) respBody.get("data");
                if (data != null && !data.isEmpty()) {
                    java.util.Map first = (java.util.Map) data.get(0);
                    java.util.List<Double> emb = (java.util.List<Double>) first.get("embedding");
                    return emb;
                }
            }
            throw new RuntimeException("Embedding API 返回格式异常");
        } catch (Exception e) {
            throw new RuntimeException("Embedding 调用失败: " + e.getMessage());
        }
    }

    /** 将 List<Double> 转为 JSON 字符串存入数据库 */
    private String embeddingToJson(java.util.List<Double> emb) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < emb.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(emb.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    /** 将 JSON 字符串解析为 List<Double> */
    private java.util.List<Double> jsonToEmbedding(String json) {
        java.util.List<Double> result = new java.util.ArrayList<>();
        String trimmed = json.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String inner = trimmed.substring(1, trimmed.length() - 1);
            if (!inner.isEmpty()) {
                for (String s : inner.split(",")) {
                    result.add(Double.parseDouble(s.trim()));
                }
            }
        }
        return result;
    }

    /** 计算余弦相似度 */
    private double cosineSimilarity(java.util.List<Double> a, java.util.List<Double> b) {
        if (a == null || b == null || a.size() != b.size() || a.isEmpty()) return 0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /** 为商品生成并保存向量 (幂等) */
    private void ensureVector(Goods g) {
        if (g == null) return;
        com.example.entity.GoodsVector existing = goodsVectorMapper.findByGoodsId(g.getId());
        if (existing != null && existing.getEmbedding() != null) return;

        String text = g.getTitle()
            + " " + (g.getType() != null ? g.getType() : "")
            + " " + (g.getDescription() != null ? g.getDescription() : "");
        java.util.List<Double> emb = getEmbedding(text);
        String json = embeddingToJson(emb);

        if (existing == null) {
            com.example.entity.GoodsVector gv = new com.example.entity.GoodsVector();
            gv.setGoodsId(g.getId());
            gv.setEmbedding(json);
            goodsVectorMapper.insert(gv);
        } else {
            goodsVectorMapper.update(existing);
        }
    }

    @PostMapping("/goods/ai-valuation")
    public Result<String> aiValuation(@RequestBody java.util.Map<String, Object> body) {
        String title = (String) body.get("title");
        String type = (String) body.get("type");
        String description = (String) body.get("description");
        Object priceObj = body.get("startPrice");
        boolean hasStartPrice = priceObj != null;
        String startPrice = hasStartPrice ? priceObj.toString() : "";

        if (title == null || title.trim().isEmpty()) {
            return Result.error(400, "请提供商品名称");
        }
        if (type == null || type.trim().isEmpty()) {
            return Result.error(400, "请选择商品类型");
        }

        // 构建查询文本用于向量检索
        String queryText = title + " " + type + " " + (description != null ? description : "");

        try {
            // 1. 获取所有已成交/在售商品（作为候选池）
            java.util.List<Goods> candidates = goodsService.listActive(null, null);
            // 补充已成交商品
            java.util.List<Goods> soldGoods = goodsService.listSoldByType(null, 100);
            if (soldGoods != null) {
                java.util.Set<Long> existingIds = new java.util.HashSet<>();
                if (candidates != null) for (Goods g : candidates) existingIds.add(g.getId());
                for (Goods g : soldGoods) {
                    if (!existingIds.contains(g.getId())) {
                        if (candidates == null) candidates = new java.util.ArrayList<>();
                        candidates.add(g);
                    }
                }
            }
            if (candidates == null || candidates.isEmpty()) {
                candidates = new java.util.ArrayList<>();
            }

            // 2. 确保所有候选商品都有向量（首次调用时生成，后续复用）
            for (Goods g : candidates) {
                try { ensureVector(g); } catch (Exception e) {
                    System.err.println("生成向量失败 goodsId=" + g.getId() + ": " + e.getMessage());
                }
            }

            // 3. 获取查询向量
            java.util.List<Double> queryEmb = getEmbedding(queryText);

            // 4. 计算相似度并排序
            java.util.List<com.example.entity.GoodsWithScore> scored = new java.util.ArrayList<>();
            java.util.List<com.example.entity.GoodsVector> allVectors = goodsVectorMapper.findAll();
            java.util.Map<Long, com.example.entity.GoodsVector> vecMap = new java.util.HashMap<>();
            if (allVectors != null) for (com.example.entity.GoodsVector v : allVectors) vecMap.put(v.getGoodsId(), v);

            for (Goods g : candidates) {
                com.example.entity.GoodsVector v = vecMap.get(g.getId());
                if (v != null && v.getEmbedding() != null) {
                    try {
                        java.util.List<Double> vec = jsonToEmbedding(v.getEmbedding());
                        double score = cosineSimilarity(queryEmb, vec);
                        scored.add(new com.example.entity.GoodsWithScore(g, score));
                    } catch (Exception e) {
                        // skip
                    }
                }
            }

            // 按相似度降序排列，取 top 8
            java.util.Collections.sort(scored, (a, b) -> Double.compare(b.getScore(), a.getScore()));
            int topN = Math.min(8, scored.size());
            StringBuilder similarInfo = new StringBuilder();
            similarInfo.append("向量检索到的同类历史成交记录（按相似度排序）：\n");
            int rank = 0;
            for (int i = 0; i < topN; i++) {
                com.example.entity.GoodsWithScore gws = scored.get(i);
                Goods g = gws.getGoods();
                rank++;
                similarInfo.append("#").append(rank)
                    .append(" [相似度:").append(String.format("%.2f", gws.getScore())).append("]")
                    .append(" ").append(g.getTitle())
                    .append(" | 类型:").append(g.getType())
                    .append(" | 起拍价:").append(g.getStartPrice())
                    .append(" | 当前价:").append(g.getCurrentPrice());
                if (g.getDescription() != null && !g.getDescription().isEmpty()) {
                    similarInfo.append(" | 描述:").append(g.getDescription());
                }
                similarInfo.append("\n");
            }
            if (rank == 0) {
                similarInfo.append("暂无历史成交记录。\n");
            }

            // 5. LLM 生成估价建议
            String systemPrompt = "你是一个拍卖行AI估价助手。请根据用户提供的商品信息和向量检索到的同类历史成交数据，"
                + "用中文回复，给出估价区间建议和定价策略分析。"
                + "格式要求：用简洁的段落，先给出建议起拍价区间（如'建议起拍价区间：XXX元 ~ YYY元'），"
                + "再分析定价策略（2-3句话）。不要使用markdown格式。";

            String userPrompt = "商品信息：\n"
                + "名称：" + title + "\n"
                + "类型：" + type + "\n"
                + (hasStartPrice ? "起拍价：" + startPrice + "元\n" : "")
                + "描述：" + (description != null ? description : "") + "\n\n"
                + similarInfo.toString()
                + "\n请根据以上信息给出估价区间建议及定价策略分析。";

            String apiKey = env.getProperty("ai.dashscope.api-key");
            String model = env.getProperty("ai.dashscope.model", "qwen-plus");

            String result = callDashScope(apiKey, model, systemPrompt, userPrompt);
            if (result != null) {
                // 保存会话
                String sessionId = java.util.UUID.randomUUID().toString().substring(0, 8);
                java.util.List<java.util.Map<String, String>> msgs = new java.util.ArrayList<>();
                java.util.Map<String, String> sysMsg = new java.util.HashMap<>();
                sysMsg.put("role", "system");
                sysMsg.put("content", systemPrompt + "\n\n检索到的历史数据：\n" + similarInfo.toString());
                msgs.add(sysMsg);
                java.util.Map<String, String> uMsg = new java.util.HashMap<>();
                uMsg.put("role", "user");
                uMsg.put("content", userPrompt);
                msgs.add(uMsg);
                chatSessions.put(sessionId, msgs);
                return Result.ok("【会话ID: " + sessionId + "】\n" + result.trim());
            }
            return Result.error("AI 估价失败，请稍后重试");
        } catch (Exception e) {
            return Result.error(500, "AI 估价调用失败: " + e.getMessage());
        }
    }

    @PostMapping("/goods/ai-valuation/chat")
    public Result<String> aiValuationChat(@RequestBody java.util.Map<String, Object> body) {
        String sessionId = (String) body.get("sessionId");
        String message = (String) body.get("message");

        if (sessionId == null || message == null || message.trim().isEmpty()) {
            return Result.error(400, "参数不完整");
        }

        java.util.List<java.util.Map<String, String>> history = chatSessions.get(sessionId);
        if (history == null) {
            return Result.error(404, "对话会话不存在或已过期，请重新估价");
        }

        try {
            String apiKey = env.getProperty("ai.dashscope.api-key");
            String model = env.getProperty("ai.dashscope.model", "qwen-plus");

            java.util.Map<String, String> userMsg = new java.util.HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message.trim());
            history.add(userMsg);

            java.util.List<java.util.Map<String, String>> messages = new java.util.ArrayList<>();
            java.util.Map<String, String> sysMsg = history.get(0);
            messages.add(sysMsg);
            int start = Math.max(1, history.size() - 9);
            for (int i = start; i < history.size(); i++) {
                messages.add(history.get(i));
            }

            String result = callDashScope(apiKey, model, null, null, messages);
            if (result != null) {
                java.util.Map<String, String> aiMsg = new java.util.HashMap<>();
                aiMsg.put("role", "assistant");
                aiMsg.put("content", result.trim());
                history.add(aiMsg);
                return Result.ok(result.trim());
            }
            return Result.error("AI 回复失败，请稍后重试");
        } catch (Exception e) {
            return Result.error(500, "AI 回复失败: " + e.getMessage());
        }
    }

    /** 简化的 DashScope 调用 (单轮) */
    private String callDashScope(String apiKey, String model, String systemPrompt, String userPrompt) {
        java.util.List<java.util.Map<String, String>> messages = new java.util.ArrayList<>();
        if (systemPrompt != null) {
            java.util.Map<String, String> sysMsg = new java.util.HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);
        }
        java.util.Map<String, String> userMsg = new java.util.HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);
        return callDashScope(apiKey, model, null, null, messages);
    }

    /** 简化的 DashScope 调用 (多轮) */
    private String callDashScope(String apiKey, String model, String systemPrompt, String userPrompt,
                                  java.util.List<java.util.Map<String, String>> messages) {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> req =
                new org.springframework.http.HttpEntity<>(requestBody, headers);

            org.springframework.http.ResponseEntity<java.util.Map> resp = restTemplate.postForEntity(
                "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions",
                req,
                java.util.Map.class
            );

            java.util.Map respBody = resp.getBody();
            if (respBody != null && respBody.containsKey("choices")) {
                java.util.List choices = (java.util.List) respBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    java.util.Map choice = (java.util.Map) choices.get(0);
                    java.util.Map message = (java.util.Map) choice.get("message");
                    return (String) message.get("content");
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage());
        }
    }
}