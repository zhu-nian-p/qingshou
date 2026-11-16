package com.campus.market.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.BusinessException;
import com.campus.market.order.entity.Order;
import com.campus.market.order.mapper.OrderMapper;
import com.campus.market.review.dto.AddReviewRequest;
import com.campus.market.review.dto.ReviewVO;
import com.campus.market.review.entity.Review;
import com.campus.market.review.mapper.ReviewMapper;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评价服务：订单完成后买家评价卖家，同时调整卖家信用分。
 * <p>
 * 信用分规则：变化值 = 星级 - 3
 * <ul>
 *   <li>5 星 +2，4 星 +1，3 星不变，2 星 -1，1 星 -2</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int ORDER_COMPLETED = 1;

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Transactional
    public ReviewVO addReview(Long reviewerId, AddReviewRequest request) {
        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        if (!order.getBuyerId().equals(reviewerId)) {
            throw BusinessException.forbidden("只有买家可以评价卖家");
        }
        if (order.getStatus() != ORDER_COMPLETED) {
            throw BusinessException.badRequest("交易完成后才能评价");
        }

        boolean alreadyReviewed = reviewMapper.exists(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, order.getId())
                .eq(Review::getReviewerId, reviewerId));
        if (alreadyReviewed) {
            throw BusinessException.badRequest("你已经评价过这笔订单");
        }

        Review review = new Review();
        review.setOrderId(order.getId());
        review.setReviewerId(reviewerId);
        review.setTargetUserId(order.getSellerId());
        review.setRating(request.getRating());
        review.setContent(StringUtils.hasText(request.getContent()) ? request.getContent().trim() : null);
        reviewMapper.insert(review);

        int creditDelta = request.getRating() - 3;
        if (creditDelta != 0) {
            userMapper.addCreditScore(order.getSellerId(), creditDelta);
        }

        return toVO(reviewMapper.selectById(review.getId()), creditDelta);
    }

    /** 某个用户收到的评价列表（详情页卖家评价） */
    public List<ReviewVO> listByTargetUser(Long targetUserId) {
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getTargetUserId, targetUserId)
                .orderByDesc(Review::getCreateTime));
        return withReviewerNames(reviews);
    }

    /** 我发出的评价（前端用来判断订单是否已评价） */
    public List<ReviewVO> listByReviewer(Long reviewerId) {
        return reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                        .eq(Review::getReviewerId, reviewerId)
                        .orderByDesc(Review::getCreateTime))
                .stream()
                .map(review -> toVO(review, null))
                .toList();
    }

    /** 批量补齐评价人用户名 */
    private List<ReviewVO> withReviewerNames(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return List.of();
        }
        List<Long> reviewerIds = reviews.stream().map(Review::getReviewerId).distinct().toList();
        Map<Long, String> names = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getId, reviewerIds))
                .stream()
                .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));

        return reviews.stream().map(review -> {
            ReviewVO vo = toVO(review, null);
            vo.setReviewerName(names.get(review.getReviewerId()));
            return vo;
        }).toList();
    }

    private ReviewVO toVO(Review review, Integer creditDelta) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setReviewerId(review.getReviewerId());
        vo.setTargetUserId(review.getTargetUserId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setCreditDelta(creditDelta);
        vo.setCreateTime(review.getCreateTime());
        return vo;
    }
}
