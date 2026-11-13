package com.campus.market.topup.controller;

import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import com.campus.market.security.UserContext;
import com.campus.market.topup.dto.ApplyTopUpRequest;
import com.campus.market.topup.dto.TopUpApplyVO;
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

    /** 申请置顶：金额由后端按阶梯价计算（1天1元 / 7天5元 / 30天12元） */
    @PostMapping("/apply")
    public Result<TopUpApplyVO> apply(@Valid @RequestBody ApplyTopUpRequest request) {
        return Result.success("青售提示：申请已提交，等待管理员审核", topUpService.apply(currentUserId(), request));
    }

    /** 我的置顶申请 */
    @GetMapping("/my")
    public Result<List<TopUpApplyVO>> myApplies() {
        return Result.success(topUpService.myApplies(currentUserId()));
    }

    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        return userId;
    }
}
