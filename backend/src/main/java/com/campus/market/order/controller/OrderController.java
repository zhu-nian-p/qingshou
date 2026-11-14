package com.campus.market.order.controller;

import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import com.campus.market.order.dto.CreateOrderRequest;
import com.campus.market.order.dto.OrderVO;
import com.campus.market.order.service.OrderService;
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

/**
 * 订单接口。全部需要登录（JwtInterceptor 会拦截 /api/order/**）。
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 买家下单 */
    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody CreateOrderRequest request) {
        return Result.success("青售提示：下单成功，等待卖家确认", orderService.create(currentUserId(), request));
    }

    /** 卖家确认交易：3=待卖家确认 -> 0=待面交 */
    @PostMapping("/{id}/seller-confirm")
    public Result<OrderVO> sellerConfirm(@PathVariable Long id) {
        return Result.success("已确认交易，请与买家约定面交", orderService.sellerConfirm(id, currentUserId()));
    }

    /** 买家确认面交：0=待面交 -> 4=待卖家确认完成（双方确认后才算完成） */
    @PostMapping("/{id}/complete")
    public Result<OrderVO> complete(@PathVariable Long id) {
        return Result.success("青售提示：已确认面交，等待卖家确认完成后生效",
                orderService.buyerComplete(id, currentUserId()));
    }

    /** 卖家确认完成：4=待卖家确认完成 -> 1=已完成（此时才加信用分） */
    @PostMapping("/{id}/seller-complete")
    public Result<OrderVO> sellerComplete(@PathVariable Long id) {
        return Result.success("青售提示：交易完成，双方信用分 +1",
                orderService.sellerComplete(id, currentUserId()));
    }

    /** 卖家拒绝接单：订单取消（2），商品恢复在售（0） */
    @PostMapping("/{id}/seller-reject")
    public Result<OrderVO> sellerReject(@PathVariable Long id) {
        return Result.success("青售提示：已拒绝接单，商品已恢复在售", orderService.sellerReject(id, currentUserId()));
    }

    /** 买家：我买到的订单 */
    @GetMapping("/buyer")
    public Result<List<OrderVO>> buyerOrders() {
        return Result.success(orderService.listByBuyer(currentUserId()));
    }

    /** 卖家：我卖出的订单 */
    @GetMapping("/seller")
    public Result<List<OrderVO>> sellerOrders() {
        return Result.success(orderService.listBySeller(currentUserId()));
    }

    /**
     * 拦截器已经保证 /api/order/** 必须登录，这里再兜一层，避免将来路径调整后空指针。
     */
    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        return userId;
    }
}
