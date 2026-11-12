package com.campus.market.item.controller;

import com.campus.market.common.Result;
import com.campus.market.item.dto.ItemDetailVO;
import com.campus.market.item.dto.ItemListVO;
import com.campus.market.item.dto.PublishItemRequest;
import com.campus.market.item.dto.PublishItemResponse;
import com.campus.market.item.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /** 发布商品：必须登录，sellerId 取自 JWT */
    @PostMapping
    public Result<PublishItemResponse> publish(@Valid @RequestBody PublishItemRequest request) {
        return Result.success("青售提示：发布成功", itemService.publish(request));
    }

    /**
     * 商品列表 / 搜索。可匿名访问。
     *
     * @param keyword  标题关键词，可空
     * @param category 分类，可空
     * @param sort     price_asc / price_desc，不传则按最新
     */
    @GetMapping("/list")
    public Result<List<ItemListVO>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String category,
                                         @RequestParam(required = false) String sort) {
        return Result.success(itemService.list(keyword, category, sort));
    }

    /**
     * 商品详情。可匿名访问，但只有登录用户能看到卖家联系方式。
     */
    @GetMapping("/{id}")
    public Result<ItemDetailVO> detail(@PathVariable Long id) {
        return Result.success(itemService.detail(id));
    }
}
