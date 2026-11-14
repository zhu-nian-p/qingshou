package com.campus.market.report.controller;

import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import com.campus.market.common.ratelimit.RateLimit;
import com.campus.market.report.dto.CreateReportRequest;
import com.campus.market.report.dto.ReportVO;
import com.campus.market.report.service.ReportService;
import com.campus.market.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 用户举报（需登录，1 分钟最多 5 次） */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @RateLimit(time = 60, count = 5)
    @PostMapping
    public Result<ReportVO> create(@Valid @RequestBody CreateReportRequest request) {
        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("登录后才能举报");
        }
        return Result.success("青售提示：举报已提交，管理员会尽快处理", reportService.create(userId, request));
    }
}
