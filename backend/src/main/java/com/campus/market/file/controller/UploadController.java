package com.campus.market.file.controller;

import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import com.campus.market.file.service.UploadQuotaService;
import com.campus.market.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 本地图片上传。
 * <p>
 * 需要登录（/api/** 由 JwtInterceptor 保护）。
 * 文件存到 app.upload-dir（默认 backend/uploads），通过 /uploads/** 静态映射访问。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    private final UploadQuotaService uploadQuotaService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("登录后才能上传图片");
        }
        // 配额检查：每个用户最多 50 张
        uploadQuotaService.ensureQuota(userId);

        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("请选择要上传的图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw BusinessException.badRequest("只允许上传图片文件");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            extension = original.substring(dot + 1).toLowerCase(Locale.ROOT);
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw BusinessException.badRequest("只支持 jpg / jpeg / png / gif / webp / bmp 格式");
        }

        // 用 UUID 重命名，避免中文名、重名和路径穿越
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
        } catch (IOException ex) {
            throw BusinessException.badRequest("图片保存失败，请重试");
        }

        String relativeUrl = "/uploads/" + filename;
        String fullUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(relativeUrl)
                .toUriString();
        uploadQuotaService.record(userId, relativeUrl, filename, file.getSize());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("url", relativeUrl);
        data.put("fullUrl", fullUrl);
        data.put("filename", filename);
        data.put("size", file.getSize());
        return Result.success("青售提示：上传成功", data);
    }
}
