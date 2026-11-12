-- ============================================================================
--  005：新增置顶购买订单表 top_up_order（置顶变现）
-- ----------------------------------------------------------------------------
--  流程：用户申请置顶（status=0 待审核） -> 管理员审核通过（status=1）
--        -> 商品 is_top=1、top_expire_time=now+days
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/005_top_up_order.sql
-- ============================================================================

USE `campus_market`;

CREATE TABLE IF NOT EXISTS `top_up_order` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '置顶订单ID，主键，自增',
  `item_id`     BIGINT UNSIGNED NOT NULL                COMMENT '要置顶的商品ID，关联 item.id',
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '下单用户ID（必须是商品所有者），关联 user.id',
  `days`        INT             NOT NULL                COMMENT '购买置顶天数，1~30',
  `price`       DECIMAL(10,2)   NOT NULL                COMMENT '应付金额（元），按 单价/天 × 天数 计算',
  `status`      TINYINT         NOT NULL DEFAULT 0      COMMENT '置顶订单状态：0=待审核，1=已支付/已生效，2=已驳回',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_topup_item`   (`item_id`) COMMENT '按商品查置顶记录',
  KEY `idx_topup_user`   (`user_id`, `create_time`) COMMENT '按用户查我的置顶订单',
  KEY `idx_topup_status` (`status`, `create_time`) COMMENT '管理员按状态查待审核列表',
  CONSTRAINT `fk_topup_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE  ON UPDATE RESTRICT,
  CONSTRAINT `fk_topup_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_topup_status` CHECK (`status` IN (0, 1, 2)),
  CONSTRAINT `chk_topup_days`   CHECK (`days` BETWEEN 1 AND 30),
  CONSTRAINT `chk_topup_price`  CHECK (`price` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '置顶购买订单表：用户申请置顶 -> 管理员审核 -> 商品置顶生效';
