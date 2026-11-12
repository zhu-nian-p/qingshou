package com.campus.market.topup.controller;

import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import com.campus.market.security.UserContext;
import com.campus.market.topup.dto.TopUpOrderVO;
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

    /** 待审核列表 */
    @GetMapping("/pending")
    public Result<List<TopUpOrderVO>> pending() {
        return Result.success(topUpService.pendingOrders(currentUserId()));
    }

    /** 审核通过：订单变已支付，商品置顶生效 */
    @PostMapping("/{id}/approve")
    public Result<TopUpOrderVO> approve(@PathVariable Long id) {
        return Result.success("青售提示：审核通过，商品已置顶", topUpService.approve(currentUserId(), id));
    }

    /** 驳回 */
    @PostMapping("/{id}/reject")
    public Result<TopUpOrderVO> reject(@PathVariable Long id) {
        return Result.success("青售提示：已驳回该置顶申请", topUpService.reject(currentUserId(), id));
    }

    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        return userId;
    }
}
