package com.example.controller;

import com.example.common.Result;
import com.example.entity.AuctionAdmin;
import com.example.entity.SysConfig;
import com.example.service.AuctionAdminService;
import com.example.service.SysConfigService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys")
public class SysAdminController {

    @Autowired private UserService userService;
    @Autowired private AuctionAdminService auctionAdminService;
    @Autowired private SysConfigService sysConfigService;

    // ========== 用户管理 ==========
    @GetMapping("/users")
    public Result<Map<String, Object>> users(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String role,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userService.page(keyword, role, status, page, size));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    // ========== 拍卖管理员 ==========
    @GetMapping("/auction-admins")
    public Result<List<AuctionAdmin>> auctionAdmins(@RequestParam(required = false) String keyword) {
        return Result.ok(auctionAdminService.list(keyword));
    }

    @PostMapping("/auction-admins")
    public Result<Void> addAuctionAdmin(@RequestBody AuctionAdmin a) {
        auctionAdminService.add(a);
        return Result.ok();
    }

    @PutMapping("/auction-admins/{id}/status")
    public Result<Void> updateAdminStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        auctionAdminService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    @DeleteMapping("/auction-admins/{id}")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        auctionAdminService.delete(id);
        return Result.ok();
    }

    // ========== 系统参数 ==========
    @GetMapping("/config")
    public Result<SysConfig> getConfig() { return Result.ok(sysConfigService.get()); }

    @PutMapping("/config")
    public Result<Void> saveConfig(@RequestBody SysConfig cfg) {
        sysConfigService.save(cfg);
        return Result.ok();
    }
}
