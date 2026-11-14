package com.campus.market.report.controller;

import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import com.campus.market.report.dto.HandleReportRequest;
import com.campus.market.report.dto.ReportVO;
import com.campus.market.report.service.ReportService;
import com.campus.market.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 管理后台举报处理 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping
    public Result<List<ReportVO>> pending() {
        return Result.success(reportService.pending(currentUserId()));
    }

    @PostMapping("/{id}/handle")
    public Result<ReportVO> handle(@PathVariable Long id,
                                   @Valid @RequestBody HandleReportRequest request) {
        return Result.success("青售提示：举报已处理",
                reportService.handle(currentUserId(), id, request.getAction()));
    }

    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        return userId;
    }
}
