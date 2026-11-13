-- ============================================================================
--  008：置顶申请改为 top_up_apply（阶梯定价：1天1元 / 7天5元 / 30天12元）
-- ----------------------------------------------------------------------------
--  旧表 top_up_order 采用线性计价，已由本表替代；旧表无业务数据，直接删除。
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/008_top_up_apply.sql
-- ============================================================================

USE `campus_market`;

CREATE TABLE IF NOT EXISTS `top_up_apply` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '置顶申请ID，主键，自增',
  `item_id`     BIGINT UNSIGNED NOT NULL                COMMENT '要置顶的商品ID，关联 item.id',
  `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '申请人（必须是商品所有者），关联 user.id',
  `days`        INT             NOT NULL                COMMENT '置顶天数，只允许 1 / 7 / 30',
  `amount`      DECIMAL(10,2)   NOT NULL                COMMENT '应付金额（元），由后端按阶梯价计算，不信任前端',
  `status`      TINYINT         NOT NULL DEFAULT 0      COMMENT '状态：0=待审核，1=已通过（已置顶），2=已拒绝',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`),
  KEY `idx_topup_apply_item`   (`item_id`)                  COMMENT '按商品查申请',
  KEY `idx_topup_apply_user`   (`user_id`, `create_time`)   COMMENT '我的置顶申请',
  KEY `idx_topup_apply_status` (`status`, `create_time`)    COMMENT '管理员待审核列表',
  CONSTRAINT `fk_topup_apply_item` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE  ON UPDATE RESTRICT,
  CONSTRAINT `fk_topup_apply_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_topup_apply_status` CHECK (`status` IN (0, 1, 2)),
  CONSTRAINT `chk_topup_apply_days`   CHECK (`days` IN (1, 7, 30)),
  CONSTRAINT `chk_topup_apply_amount` CHECK (`amount` >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '置顶申请表：阶梯定价申请 -> 管理员审核 -> 商品置顶';

-- 旧表已被替代（无业务数据）
DROP TABLE IF EXISTS `top_up_order`;
