package com.campus.market.review.controller;

import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import com.campus.market.review.dto.AddReviewRequest;
import com.campus.market.review.dto.ReviewVO;
import com.campus.market.review.service.ReviewService;
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

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** 买家评价卖家（订单必须已完成） */
    @PostMapping
    public Result<ReviewVO> add(@Valid @RequestBody AddReviewRequest request) {
        return Result.success("青售提示：评价成功", reviewService.addReview(currentUserId(), request));
    }

    /** 查看某个用户收到的评价 */
    @GetMapping("/user/{userId}")
    public Result<List<ReviewVO>> listByUser(@PathVariable Long userId) {
        return Result.success(reviewService.listByTargetUser(userId));
    }

    /** 我发出的评价（用于判断哪些订单已评价） */
    @GetMapping("/mine")
    public Result<List<ReviewVO>> mine() {
        return Result.success(reviewService.listByReviewer(currentUserId()));
    }

    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        return userId;
    }
}
