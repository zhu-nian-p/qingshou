package com.campus.market.item.controller;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.common.ratelimit.RateLimit;
import com.campus.market.item.dto.ContactInfoVO;
import com.campus.market.item.dto.ItemDetailVO;
import com.campus.market.item.dto.ItemListVO;
import com.campus.market.item.dto.ItemUpdateRequest;
import com.campus.market.item.dto.PublishItemRequest;
import com.campus.market.item.dto.PublishItemResponse;
import com.campus.market.item.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /** 发布商品：必须登录，sellerId 取自 JWT；每分钟最多 5 次 */
    @RateLimit(time = 60, count = 5)
    @PostMapping
    public Result<PublishItemResponse> publish(@Valid @RequestBody PublishItemRequest request) {
        return Result.success("青售提示：发布成功", itemService.publish(request));
    }

    /**
     * 商品列表 / 搜索（分页）。可匿名访问。
     *
     * @param keyword  标题关键词，可空
     * @param category 分类，可空
     * @param campus   校区（全部/上海路/前湖），可空
     * @param sort     price_asc / price_desc，不传则按最新
     * @param page     页码，默认 1
     * @param size     每页条数，默认 10（最大 50）
     */
    @GetMapping("/list")
    public Result<PageResult<ItemListVO>> list(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String category,
                                               @RequestParam(required = false) String campus,
                                               @RequestParam(required = false) String sort,
                                               @RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size) {
        return Result.success(itemService.list(keyword, category, campus, sort, page, size));
    }

    /** 编辑在售商品（卖家本人） */
    @PutMapping("/{id}")
    public Result<ItemDetailVO> update(@PathVariable Long id,
                                       @Valid @RequestBody ItemUpdateRequest request) {
        return Result.success("青售提示：修改成功", itemService.update(id, request));
    }

    /** 下架在售商品（卖家本人） */
    @PostMapping("/{id}/offline")
    public Result<ItemDetailVO> offline(@PathVariable Long id) {
        return Result.success("青售提示：商品已下架", itemService.offline(id));
    }

    /**
     * 获取完整联系方式：必须登录，同一用户 1 分钟最多 3 次。
     * 详情接口只返回掩码，只有用户主动复制时才调这里拿完整值。
     */
    @RateLimit(time = 60, count = 3)
    @GetMapping("/{id}/contact")
    public Result<ContactInfoVO> contact(@PathVariable Long id) {
        return Result.success(new ContactInfoVO(itemService.contact(id)));
    }

    /**
     * 商品详情。可匿名访问，但只有登录用户能看到卖家联系方式。
     */
    @GetMapping("/{id}")
    public Result<ItemDetailVO> detail(@PathVariable Long id) {
        return Result.success(itemService.detail(id));
    }
}
