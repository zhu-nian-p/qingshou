package com.campus.market.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.admin.dto.AdminUserVO;
import com.campus.market.common.BusinessException;
import com.campus.market.item.dto.ItemListVO;
import com.campus.market.item.entity.Item;
import com.campus.market.item.mapper.ItemMapper;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员能力：置顶/取消置顶商品、封禁/解封用户。
 * <p>
 * 角色校验放在这里：role != 1 一律 403。
 * 真实项目里建议换成 Spring Security + @PreAuthorize，这里用最直白的方式实现。
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int ROLE_ADMIN = 2;
    private static final int STATUS_BANNED = 1;
    private static final int STATUS_NORMAL = 0;

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    /** 置顶商品，days 天后自动过期 */
    public ItemListVO topItem(Long adminId, Long itemId, int days) {
        requireAdmin(adminId);
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }
        int validDays = Math.max(1, Math.min(days, 365));
        LocalDateTime expireTime = LocalDateTime.now().plusDays(validDays);
        itemMapper.updateTop(itemId, 1, expireTime);
        return toItemVO(itemMapper.selectById(itemId));
    }

    /** 取消置顶 */
    public ItemListVO untopItem(Long adminId, Long itemId) {
        requireAdmin(adminId);
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }
        itemMapper.updateTop(itemId, 0, null);
        return toItemVO(itemMapper.selectById(itemId));
    }

    /** 全部用户（管理后台用户封禁列表） */
    public List<AdminUserVO> listUsers(Long adminId) {
        requireAdmin(adminId);
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                        .orderByAsc(User::getId))
                .stream()
                .map(this::toUserVO)
                .toList();
    }

    /** 封禁用户 */
    public AdminUserVO banUser(Long adminId, Long userId) {
        requireAdmin(adminId);
        User target = requireTarget(userId);
        if (target.getId().equals(adminId)) {
            throw BusinessException.badRequest("不能封禁自己");
        }
        if (target.getRole() != null && target.getRole() == ROLE_ADMIN) {
            throw BusinessException.badRequest("不能封禁管理员账号");
        }        userMapper.updateStatus(userId, STATUS_BANNED);
        return toUserVO(userMapper.selectById(userId));
    }

    /** 解封用户 */
    public AdminUserVO unbanUser(Long adminId, Long userId) {
        requireAdmin(adminId);
        User target = requireTarget(userId);
        userMapper.updateStatus(userId, STATUS_NORMAL);
        return toUserVO(userMapper.selectById(userId));
    }

    /** 供其他服务复用的管理员校验：不是管理员直接 403 */
    public void requireAdmin(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        if (user.getRole() == null || user.getRole() != ROLE_ADMIN) {
            throw BusinessException.forbidden("需要管理员权限");
        }
    }

    private User requireTarget(Long userId) {
        User target = userMapper.selectById(userId);
        if (target == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return target;
    }

    private ItemListVO toItemVO(Item item) {
        ItemListVO vo = new ItemListVO();
        vo.setId(item.getId());
        vo.setSellerId(item.getSellerId());
        vo.setTitle(item.getTitle());
        vo.setPrice(item.getPrice());
        vo.setImageUrl(item.getImageUrl());
        vo.setCategory(item.getCategory());
        vo.setStatus(item.getStatus());
        vo.setIsTop(item.getIsTop());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    private AdminUserVO toUserVO(User user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setStudentId(user.getStudentId());
        vo.setSchoolEmail(user.getSchoolEmail());
        vo.setStatus(user.getStatus());
        vo.setRole(user.getRole());
        vo.setCreditScore(user.getCreditScore());
        return vo;
    }
}
