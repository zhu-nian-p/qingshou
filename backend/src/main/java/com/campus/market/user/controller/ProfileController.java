package com.campus.market.user.controller;

import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import com.campus.market.item.dto.ItemListVO;
import com.campus.market.item.service.ItemService;
import com.campus.market.order.dto.OrderVO;
import com.campus.market.order.service.OrderService;
import com.campus.market.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 个人中心：我的发布、我的交易。
 * 路径都在 /api/user/me 下，必须登录。
 */
@RestController
@RequestMapping("/api/user/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ItemService itemService;
    private final OrderService orderService;

    /** 我的发布：全部状态的商品 */
    @GetMapping("/items")
    public Result<List<ItemListVO>> myItems() {
        return Result.success(itemService.listBySeller(currentUserId()));
    }

    /** 我的交易：我买到的 + 我卖出的 */
    @GetMapping("/trades")
    public Result<Map<String, List<OrderVO>>> myTrades() {
        Long userId = currentUserId();
        Map<String, List<OrderVO>> data = new LinkedHashMap<>();
        data.put("bought", orderService.listByBuyer(userId));
        data.put("sold", orderService.listBySeller(userId));
        return Result.success(data);
    }

    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        return userId;
    }
}
