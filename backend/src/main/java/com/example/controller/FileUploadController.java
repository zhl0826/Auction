package com.example.controller;

import com.example.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOW_EXTS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    @PostMapping("/image")
    public Result<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return Result.error(400, "文件为空");
        if (file.getSize() > MAX_SIZE) return Result.error(400, "文件不能超过 5MB");
        String orig = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String ext = "";
        int dot = orig.lastIndexOf(".");
        if (dot >= 0) ext = orig.substring(dot);
        if (ext.isEmpty() || !contains(ALLOW_EXTS, ext)) return Result.error(400, "仅支持 jpg/png/gif/webp");

        // 按月份分目录
        String ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        File dir = new File("uploads" + File.separator + ym);
        if (!dir.exists() && !dir.mkdirs()) return Result.error(500, "创建目录失败");
        // 文件名: uuid + ext
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        File dest = new File(dir, name);
        // 用 NIO 自己写文件, 避免 transferTo 在临时目录下找不到父目录的坑
        java.nio.file.Files.copy(file.getInputStream(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        String url = "/uploads/" + ym + "/" + name;
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("name", name);
        data.put("size", file.getSize());
        return Result.ok(data);
    }

    private static boolean contains(String[] arr, String s) {
        for (String x : arr) if (x.equalsIgnoreCase(s)) return true;
        return false;
    }
}
