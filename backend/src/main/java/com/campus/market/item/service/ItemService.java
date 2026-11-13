package com.campus.market.item.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.BusinessException;
import com.campus.market.common.SensitiveWordFilter;
import com.campus.market.item.dto.ItemDetailVO;
import com.campus.market.item.dto.ItemListVO;
import com.campus.market.item.dto.PublishItemRequest;
import com.campus.market.item.dto.PublishItemResponse;
import com.campus.market.item.entity.Item;
import com.campus.market.item.mapper.ItemMapper;
import com.campus.market.security.UserContext;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    /** 只有「在售」的商品出现在列表里 */
    private static final int STATUS_ON_SALE = 0;

    /** 发布时未指定校区时的默认值 */
    private static final String DEFAULT_CAMPUS = "前湖";

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    /**
     * 发布商品。sellerId 只从 JWT 上下文取，前端传了也不认。
     */
    public PublishItemResponse publish(PublishItemRequest request) {
        Long sellerId = UserContext.get();
        if (sellerId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }

        // 标题、描述都要过敏感词
        SensitiveWordFilter.check(request.getTitle(), request.getDescription());

        Item item = new Item();
        item.setSellerId(sellerId);
        item.setTitle(request.getTitle().trim());
        item.setDescription(trimToNull(request.getDescription()));
        item.setPrice(request.getPrice());
        item.setImageUrl(trimToNull(request.getImageUrl()));
        item.setCategory(request.getCategory().trim());
        item.setCampus(StringUtils.hasText(request.getCampus()) ? request.getCampus().trim() : DEFAULT_CAMPUS);
        item.setContactInfo(trimToNull(request.getContactInfo()));
        item.setStatus(STATUS_ON_SALE);
        item.setIsTop(0);
        itemMapper.insert(item);

        // 重新查一次，拿到数据库默认生成的 create_time
        Item saved = itemMapper.selectById(item.getId());
        return new PublishItemResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getPrice(),
                saved.getStatus(),
                saved.getIsTop(),
                saved.getCreateTime()
        );
    }

    /**
     * 商品列表 / 搜索。
     * <p>
     * 排序规则：<b>先按 is_top 降序</b>（置顶永远在前），再按用户选择的排序，
     * 最后用 create_time 兜底保证顺序稳定。
     *
     * @param keyword  模糊匹配标题，可空
     * @param category 分类精确匹配，可空
     * @param campus   校区精确匹配（「全部」或空表示不过滤）
     * @param sort     price_asc / price_desc / 空（默认最新）
     */
    public List<ItemListVO> list(String keyword, String category, String campus, String sort) {
        LambdaQueryWrapper<Item> query = new LambdaQueryWrapper<>();
        query.eq(Item::getStatus, STATUS_ON_SALE);

        if (StringUtils.hasText(keyword)) {
            query.like(Item::getTitle, keyword.trim());
        }
        if (StringUtils.hasText(category)) {
            query.eq(Item::getCategory, category.trim());
        }
        if (StringUtils.hasText(campus) && !"全部".equals(campus.trim())) {
            query.eq(Item::getCampus, campus.trim());
        }

        // 第一优先级：置顶在前
        query.orderByDesc(Item::getIsTop);

        // 第二优先级：用户选择的排序方式
        String sortKey = sort == null ? "" : sort.trim().toLowerCase();
        switch (sortKey) {
            case "price_asc" -> query.orderByAsc(Item::getPrice);
            case "price_desc" -> query.orderByDesc(Item::getPrice);
            default -> {
                // 默认按最新
            }
        }

        // 兜底：同价 / 同置顶状态时按时间倒序，保证结果稳定
        query.orderByDesc(Item::getCreateTime);

        return toListVOList(itemMapper.selectList(query));
    }

    /**
     * 商品详情。联系方式只对已登录用户下发。
     */
    public ItemDetailVO detail(Long id) {
        Item item = itemMapper.selectById(id);
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }

        ItemDetailVO vo = new ItemDetailVO();
        vo.setId(item.getId());
        vo.setSellerId(item.getSellerId());

        User seller = userMapper.selectById(item.getSellerId());
        if (seller != null) {
            vo.setSellerName(seller.getUsername());
            vo.setSellerCredit(seller.getCreditScore());
        }

        vo.setTitle(item.getTitle());
        vo.setDescription(item.getDescription());
        vo.setPrice(item.getPrice());
        vo.setImageUrl(item.getImageUrl());
        vo.setCategory(item.getCategory());
        vo.setCampus(item.getCampus());
        vo.setStatus(item.getStatus());
        vo.setIsTop(item.getIsTop());
        vo.setTopExpireTime(item.getTopExpireTime());
        vo.setCreateTime(item.getCreateTime());

        // 未登录（UserContext 为空）时 contactInfo 保持 null
        if (UserContext.get() != null) {
            vo.setContactInfo(item.getContactInfo());
        }
        return vo;
    }

    /**
     * 我的发布：当前用户发布的全部商品（包含在售/已预订/已售出/违规下架）。
     */
    public List<ItemListVO> listBySeller(Long sellerId) {
        return toListVOList(itemMapper.selectList(new LambdaQueryWrapper<Item>()
                .eq(Item::getSellerId, sellerId)
                .orderByDesc(Item::getCreateTime)));
    }

    /**
     * 列表 VO：批量补齐卖家用户名（展示用，不带真实姓名等敏感信息）。
     */
    private List<ItemListVO> toListVOList(List<Item> items) {
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> sellerIds = items.stream().map(Item::getSellerId).distinct().toList();
        Map<Long, User> sellerMap = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getId, sellerIds))
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        return items.stream().map(item -> {
            ItemListVO vo = toListVO(item);
            User seller = sellerMap.get(item.getSellerId());
            if (seller != null) {
                vo.setSellerName(seller.getUsername());
                vo.setSellerCredit(seller.getCreditScore());
            }
            return vo;
        }).toList();
    }

    private ItemListVO toListVO(Item item) {
        ItemListVO vo = new ItemListVO();
        vo.setId(item.getId());
        vo.setSellerId(item.getSellerId());
        vo.setTitle(item.getTitle());
        vo.setPrice(item.getPrice());
        vo.setImageUrl(item.getImageUrl());
        vo.setCategory(item.getCategory());
        vo.setCampus(item.getCampus());
        vo.setStatus(item.getStatus());
        vo.setIsTop(item.getIsTop());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
