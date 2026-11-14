package com.campus.market.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.admin.entity.AdminAuditLog;
import com.campus.market.admin.mapper.AdminAuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员操作审计：置顶、封禁、下架商品、处理举报等敏感操作统一落库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditLogMapper auditLogMapper;

    public void log(Long adminId, String action, Long targetId, String detail) {
        AdminAuditLog record = new AdminAuditLog();
        record.setAdminId(adminId);
        record.setAction(action);
        record.setTargetId(targetId);
        record.setDetail(detail);
        auditLogMapper.insert(record);
        log.info("管理员操作审计: adminId={}, action={}, targetId={}, detail={}",
                adminId, action, targetId, detail);
    }

    public void log(Long adminId, String action, Long targetId) {
        log(adminId, action, targetId, null);
    }

    public List<AdminAuditLog> recent(int limit) {
        return auditLogMapper.selectList(new LambdaQueryWrapper<AdminAuditLog>()
                .orderByDesc(AdminAuditLog::getCreateTime)
                .last("LIMIT " + Math.max(1, Math.min(limit, 200))));
    }
}
