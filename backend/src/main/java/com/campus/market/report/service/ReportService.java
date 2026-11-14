package com.campus.market.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.admin.service.AdminAuditService;
import com.campus.market.admin.service.AdminService;
import com.campus.market.common.BusinessException;
import com.campus.market.item.entity.Item;
import com.campus.market.item.mapper.ItemMapper;
import com.campus.market.item.service.ItemListCacheService;
import com.campus.market.report.dto.CreateReportRequest;
import com.campus.market.report.dto.ReportVO;
import com.campus.market.report.entity.Report;
import com.campus.market.report.mapper.ReportMapper;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 举报：用户提交 -> 管理员处理（下架商品 / 忽略），处理动作写审计日志。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final AdminService adminService;
    private final AdminAuditService auditService;
    private final ItemListCacheService cacheService;

    @Transactional
    public ReportVO create(Long reporterId, CreateReportRequest request) {
        if (request.getItemId() == null && request.getUserId() == null) {
            throw BusinessException.badRequest("请选择举报对象");
        }
        if (request.getItemId() != null) {
            Item item = itemMapper.selectById(request.getItemId());
            if (item == null) {
                throw BusinessException.notFound("举报的商品不存在");
            }
            if (item.getSellerId().equals(reporterId)) {
                throw BusinessException.badRequest("不能举报自己的商品");
            }
        }
        if (request.getUserId() != null) {
            User target = userMapper.selectById(request.getUserId());
            if (target == null) {
                throw BusinessException.notFound("举报的用户不存在");
            }
            if (target.getId().equals(reporterId)) {
                throw BusinessException.badRequest("不能举报自己");
            }
        }

        // 同一举报人对同一对象只能有一条待处理举报，避免刷举报
        List<Report> pending = reportMapper.selectList(new LambdaQueryWrapper<Report>()
                .eq(Report::getReporterId, reporterId)
                .eq(Report::getStatus, 0));
        boolean duplicated = pending.stream().anyMatch(r ->
                (request.getItemId() != null && Objects.equals(request.getItemId(), r.getItemId()))
                        || (request.getUserId() != null && Objects.equals(request.getUserId(), r.getTargetUserId())));
        if (duplicated) {
            throw BusinessException.badRequest("你已经举报过该对象，请等待管理员处理");
        }

        Report report = new Report();
        report.setItemId(request.getItemId());
        report.setTargetUserId(request.getUserId());
        report.setReporterId(reporterId);
        report.setReason(request.getReason().trim());
        report.setStatus(0);
        reportMapper.insert(report);
        return toVO(reportMapper.selectById(report.getId()));
    }

    public List<ReportVO> pending(Long adminId) {
        adminService.requireAdmin(adminId);
        return toVOList(reportMapper.selectList(new LambdaQueryWrapper<Report>()
                .eq(Report::getStatus, 0)
                .orderByDesc(Report::getCreateTime)));
    }

    @Transactional
    public ReportVO handle(Long adminId, Long reportId, String action) {
        adminService.requireAdmin(adminId);
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw BusinessException.notFound("举报不存在");
        }
        if (report.getStatus() == null || report.getStatus() != 0) {
            throw BusinessException.badRequest("该举报已处理");
        }

        if ("delist".equals(action)) {
            if (report.getItemId() == null) {
                throw BusinessException.badRequest("该举报不是针对商品，无法下架");
            }
            itemMapper.adminDelist(report.getItemId());
            cacheService.evictAll();
            auditService.log(adminId, "DELIST_ITEM", report.getItemId(),
                    "处理举报#" + reportId + "：下架违规商品");
        } else if ("dismiss".equals(action)) {
            auditService.log(adminId, "DISMISS_REPORT", reportId, "忽略举报");
        } else {
            throw BusinessException.badRequest("不支持的处理动作");
        }

        int updated = reportMapper.markHandled(reportId, adminId);
        if (updated != 1) {
            throw BusinessException.badRequest("举报状态已变化，请刷新后重试");
        }
        return toVO(reportMapper.selectById(reportId));
    }

    private List<ReportVO> toVOList(List<Report> reports) {
        if (reports.isEmpty()) {
            return List.of();
        }
        List<Long> itemIds = reports.stream().map(Report::getItemId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Item> itemMap = itemIds.isEmpty() ? Map.of()
                : itemMapper.selectList(new LambdaQueryWrapper<Item>().in(Item::getId, itemIds))
                .stream().collect(Collectors.toMap(Item::getId, Function.identity()));

        List<Long> userIds = reports.stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getReporterId(), r.getTargetUserId()))
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of()
                : userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, userIds))
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));

        return reports.stream().map(report -> toVO(report, itemMap, userMap)).toList();
    }

    private ReportVO toVO(Report report) {
        return toVO(report, Map.of(), Map.of());
    }

    private ReportVO toVO(Report report, Map<Long, Item> itemMap, Map<Long, User> userMap) {
        ReportVO vo = new ReportVO();
        vo.setId(report.getId());
        vo.setItemId(report.getItemId());
        vo.setTargetUserId(report.getTargetUserId());
        vo.setReporterId(report.getReporterId());
        vo.setReason(report.getReason());
        vo.setStatus(report.getStatus());
        vo.setStatusText(report.getStatus() != null && report.getStatus() == 0 ? "待处理" : "已处理");
        vo.setHandlerId(report.getHandlerId());
        vo.setHandleTime(report.getHandleTime());
        vo.setCreateTime(report.getCreateTime());

        Item item = report.getItemId() == null ? null : itemMap.get(report.getItemId());
        if (item == null && report.getItemId() != null) {
            item = itemMapper.selectById(report.getItemId());
        }
        if (item != null) {
            vo.setItemTitle(item.getTitle());
        }
        User reporter = userMap.get(report.getReporterId());
        if (reporter == null && report.getReporterId() != null) {
            reporter = userMapper.selectById(report.getReporterId());
        }
        if (reporter != null) {
            vo.setReporterName(reporter.getRealName() + "（" + reporter.getUsername() + "）");
        }
        User target = report.getTargetUserId() == null ? null : userMap.get(report.getTargetUserId());
        if (target != null) {
            vo.setTargetUserName(target.getUsername());
        }
        return vo;
    }
}
