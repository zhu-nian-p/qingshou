package com.campus.market.topup.controller;

import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import com.campus.market.security.UserContext;
import com.campus.market.topup.dto.TopUpApplyVO;
import com.campus.market.topup.service.TopUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 置顶订单审核（管理员侧） */
@RestController
@RequestMapping("/api/admin/topup")
@RequiredArgsConstructor
public class AdminTopUpController {

    private final TopUpService topUpService;

    /** 待审核申请列表（status=0，含商品名称） */
    @GetMapping("/pending")
    public Result<List<TopUpApplyVO>> pending() {
        return Result.success(topUpService.pendingApplies(currentUserId()));
    }

    /** 审核通过：申请状态改 1，商品置顶对应天数 */
    @PostMapping("/{id}/approve")
    public Result<TopUpApplyVO> approve(@PathVariable Long id) {
        return Result.success("青售提示：审核通过，商品已置顶", topUpService.approve(currentUserId(), id));
    }

    /** 拒绝：申请状态改 2 */
    @PostMapping("/{id}/reject")
    public Result<TopUpApplyVO> reject(@PathVariable Long id) {
        return Result.success("青售提示：已拒绝该置顶申请", topUpService.reject(currentUserId(), id));
    }

    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        return userId;
    }
}
