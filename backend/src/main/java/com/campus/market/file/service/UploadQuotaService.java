package com.campus.market.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.BusinessException;
import com.campus.market.file.entity.FileUpload;
import com.campus.market.file.mapper.FileUploadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 图片上传配额：每个用户最多上传 N 张，防止恶意刷接口把磁盘打满。
 */
@Service
@RequiredArgsConstructor
public class UploadQuotaService {

    private final FileUploadMapper fileUploadMapper;

    @Value("${app.upload.max-per-user:50}")
    private int maxPerUser;

    public long countByUser(Long userId) {
        return fileUploadMapper.selectCount(new LambdaQueryWrapper<FileUpload>()
                .eq(FileUpload::getUserId, userId));
    }

    public void ensureQuota(Long userId) {
        if (countByUser(userId) >= maxPerUser) {
            throw BusinessException.badRequest("上传图片数量已达上限（" + maxPerUser + " 张），请先复用已有图片");
        }
    }

    public void record(Long userId, String url, String filename, long size) {
        FileUpload record = new FileUpload();
        record.setUserId(userId);
        record.setUrl(url);
        record.setFilename(filename);
        record.setSize(size);
        fileUploadMapper.insert(record);
    }

    public int maxPerUser() {
        return maxPerUser;
    }
}
