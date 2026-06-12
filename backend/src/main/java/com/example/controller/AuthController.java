package com.example.controller;

import com.example.common.Result;
import com.example.entity.AuctionAdmin;
import com.example.entity.User;
import com.example.service.AuctionAdminService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private UserService userService;
    @Autowired private AuctionAdminService auctionAdminService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String role = body.get("role");
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) return Result.error(400, "请输入账号和密码");

        Map<String, Object> data = new HashMap<>();
        try {
            if ("sys_admin".equals(role)) {
                // 系统管理员演示账号写死
                if (!"sysadmin".equals(username)) return Result.error(401, "账号不存在");
                if (!"123456".equals(password)) return Result.error(401, "账号或密码错误");
                data.put("token", "sys-token-" + System.currentTimeMillis());
                data.put("role", "sys_admin");
                data.put("username", "sysadmin");
                return Result.ok(data);
            } else if ("auction_admin".equals(role)) {
                AuctionAdmin a = auctionAdminService.login(username, password);
                data.put("token", "auction-token-" + System.currentTimeMillis());
                data.put("role", "auction_admin");
                data.put("username", a.getUsername());
                data.put("nickname", a.getNickname());
                return Result.ok(data);
            } else if ("user".equals(role)) {
                // 预留：买家/卖家登录（下一阶段再做）
                User u = userService.login(username, password);
                data.put("token", "user-token-" + System.currentTimeMillis());
                data.put("role", "user");
                data.put("userId", u.getId());
                data.put("username", u.getUsername());
                data.put("nickname", u.getNickname());
                return Result.ok(data);
            }
            return Result.error(400, "未知角色");
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }
}
