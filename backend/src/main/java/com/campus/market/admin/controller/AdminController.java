package com.campus.market.admin.controller;

import com.campus.market.admin.dto.AdminUserVO;
import com.campus.market.admin.dto.TopItemRequest;
import com.campus.market.admin.service.AdminService;
import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import com.campus.market.item.dto.ItemListVO;
import com.campus.market.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台接口，全部需要管理员角色（role=1）。
 * 开发环境管理员账号：admin01 / 123456
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /** 置顶商品，默认 7 天 */
    @PostMapping("/items/{id}/top")
    public Result<ItemListVO> topItem(@PathVariable Long id,
                                      @RequestParam(defaultValue = "7") int days) {
        return Result.success("青售提示：置顶成功", adminService.topItem(currentUserId(), id, days));
    }

    /**
     * 管理员直接置顶（人工审核线下付款后手动确认）。
     * 请求体：{"itemId": 2, "days": 7}
     */
    @PostMapping("/items/top")
    public Result<ItemListVO> topItemByBody(@Valid @RequestBody TopItemRequest request) {
        return Result.success("青售提示：置顶成功",
                adminService.topItem(currentUserId(), request.getItemId(), request.getDays()));
    }

    /** 取消置顶 */
    @PostMapping("/items/{id}/untop")
    public Result<ItemListVO> untopItem(@PathVariable Long id) {
        return Result.success("青售提示：已取消置顶", adminService.untopItem(currentUserId(), id));
    }

    /** 封禁用户 */
    @PostMapping("/users/{id}/ban")
    public Result<AdminUserVO> banUser(@PathVariable Long id) {
        return Result.success("青售提示：已封禁该用户", adminService.banUser(currentUserId(), id));
    }

    /** 解封用户 */
    @PostMapping("/users/{id}/unban")
    public Result<AdminUserVO> unbanUser(@PathVariable Long id) {
        return Result.success("青售提示：已解除封禁", adminService.unbanUser(currentUserId(), id));
    }

    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        return userId;
    }
}
