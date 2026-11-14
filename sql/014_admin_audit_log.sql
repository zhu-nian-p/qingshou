-- ============================================================================
--  014：管理员操作审计日志 admin_audit_log
--  记录置顶、封禁、下架商品等敏感操作
--  执行：mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/014_admin_audit_log.sql
-- ============================================================================

USE `campus_market`;

CREATE TABLE IF NOT EXISTS `admin_audit_log` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_id`    BIGINT UNSIGNED NOT NULL                COMMENT '操作管理员用户ID',
  `action`      VARCHAR(64)     NOT NULL                COMMENT '操作类型：TOP_ITEM/BAN_USER/DELIST_ITEM 等',
  `target_id`   BIGINT UNSIGNED NULL                    COMMENT '操作对象ID（商品ID/用户ID/举报ID）',
  `detail`      VARCHAR(500)    NULL                    COMMENT '补充说明',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_audit_admin` (`admin_id`, `create_time`),
  KEY `idx_admin_audit_action` (`action`, `create_time`),
  CONSTRAINT `fk_admin_audit_admin` FOREIGN KEY (`admin_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '管理员操作审计日志';
