package com.campus.market.topup.controller;

import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import com.campus.market.security.UserContext;
import com.campus.market.topup.dto.ApplyTopUpRequest;
import com.campus.market.topup.dto.TopUpOrderVO;
import com.campus.market.topup.service.TopUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 置顶购买（用户侧） */
@RestController
@RequestMapping("/api/topup")
@RequiredArgsConstructor
public class TopUpController {

    private final TopUpService topUpService;

    /** 申请置顶：生成待审核订单，价格 = 单价 × 天数 */
    @PostMapping("/apply")
    public Result<TopUpOrderVO> apply(@Valid @RequestBody ApplyTopUpRequest request) {
        return Result.success("青售提示：申请已提交，等待管理员审核", topUpService.apply(currentUserId(), request));
    }

    /** 我的置顶订单 */
    @GetMapping("/my")
    public Result<List<TopUpOrderVO>> myOrders() {
        return Result.success(topUpService.myOrders(currentUserId()));
    }

    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        return userId;
    }
}
